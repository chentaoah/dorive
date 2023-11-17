/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gitee.dorive.query.impl.resolver;

import cn.hutool.core.lang.Assert;
import com.gitee.dorive.query.entity.QueryField;
import com.gitee.dorive.query.entity.QueryType;
import com.gitee.dorive.query.entity.MergedRepository;
import com.gitee.dorive.query.entity.SpecificFields;
import com.gitee.dorive.query.entity.def.QueryScanDef;
import com.gitee.dorive.query.entity.def.CriterionDef;
import com.gitee.dorive.query.entity.def.ExampleDef;
import com.gitee.dorive.query.repository.AbstractQueryRepository;
import com.gitee.dorive.query.util.ResourceUtils;
import com.gitee.dorive.core.repository.CommonRepository;
import lombok.Data;
import org.springframework.util.ReflectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

@Data
public class QueryTypeResolver {

    private static Map<String, List<Class<?>>> scannedClasses = new ConcurrentHashMap<>();

    private AbstractQueryRepository<?, ?> repository;
    private Map<Class<?>, QueryType> classQueryTypeMap = new ConcurrentHashMap<>();
    private Map<String, QueryType> nameQueryTypeMap = new ConcurrentHashMap<>();

    public QueryTypeResolver(AbstractQueryRepository<?, ?> repository) throws Exception {
        this.repository = repository;
        resolve();
    }

    public void resolve() throws Exception {
        QueryScanDef queryScanDef = repository.getQueryScanDef();
        String[] scanPackages = queryScanDef.getValue();
        String regex = queryScanDef.getRegex();
        Class<?>[] queries = queryScanDef.getQueries();

        Pattern pattern = Pattern.compile(regex);
        for (String scanPackage : scanPackages) {
            List<Class<?>> classes = scannedClasses.get(scanPackage);
            if (classes == null) {
                classes = ResourceUtils.resolveClasses(scanPackage);
                scannedClasses.put(scanPackage, classes);
            }
            for (Class<?> queryClass : classes) {
                String simpleName = queryClass.getSimpleName();
                if (pattern.matcher(simpleName).matches()) {
                    resolveQueryClass(queryClass);
                }
            }
        }

        for (Class<?> queryClass : queries) {
            resolveQueryClass(queryClass);
        }
    }

    private void resolveQueryClass(Class<?> queryClass) {
        ExampleDef exampleDef = ExampleDef.fromElement(queryClass);
        if (exampleDef == null) {
            return;
        }

        List<QueryField> queryFields = new ArrayList<>();
        SpecificFields specificFields = new SpecificFields();

        ReflectionUtils.doWithLocalFields(queryClass, declaredField -> {
            QueryField queryField = new QueryField(declaredField);
            if (queryField.isIgnore()) {
                return;
            }
            if (!specificFields.tryAddField(queryField)) {
                queryFields.add(queryField);
            }
        });

        List<MergedRepository> mergedRepositories = matchMergedRepositories(queryFields);
        List<MergedRepository> reversedMergedRepositories = new ArrayList<>(mergedRepositories);
        Collections.reverse(reversedMergedRepositories);

        QueryType queryType = new QueryType(exampleDef, queryFields, specificFields, mergedRepositories, reversedMergedRepositories);
        classQueryTypeMap.put(queryClass, queryType);
        nameQueryTypeMap.put(queryClass.getName(), queryType);
    }

    private List<MergedRepository> matchMergedRepositories(List<QueryField> queryFields) {
        MergedRepositoryResolver mergedRepositoryResolver = repository.getMergedRepositoryResolver();
        Map<String, MergedRepository> mergedRepositoryMap = mergedRepositoryResolver.getMergedRepositoryMap();
        Map<String, MergedRepository> nameMergedRepositoryMap = mergedRepositoryResolver.getNameMergedRepositoryMap();

        Set<MergedRepository> mergedRepositorySet = new LinkedHashSet<>();

        for (QueryField queryField : queryFields) {
            CriterionDef criterionDef = queryField.getCriterionDef();
            String belongTo = criterionDef.getBelongTo();
            String field = criterionDef.getField();

            if (!belongTo.startsWith("/")) {
                MergedRepository mergedRepository = nameMergedRepositoryMap.get(belongTo);
                Assert.notNull(mergedRepository, "No merged repository found! belongTo: {}", belongTo);
                belongTo = mergedRepository.getAbsoluteAccessPath();
                criterionDef.setBelongTo(belongTo);
            }
            MergedRepository mergedRepository = mergedRepositoryMap.get(belongTo);
            Assert.notNull(mergedRepository, "No merged repository found! belongTo: {}", belongTo);

            CommonRepository repository = mergedRepository.getExecutedRepository();
            Assert.isTrue(repository.hasField(field),
                    "The field of @Criterion does not exist in the entity! query field: {}, entity: {}, field: {}",
                    queryField.getField(), repository.getEntityEle().getElement(), field);

            mergedRepositorySet.add(mergedRepository);
        }

        for (MergedRepository mergedRepository : mergedRepositoryMap.values()) {
            CommonRepository repository = mergedRepository.getExecutedRepository();
            if (repository.isBoundEntity()) {
                mergedRepositorySet.add(mergedRepository);
            }
        }

        List<MergedRepository> mergedRepositories = new ArrayList<>(mergedRepositorySet);
        mergedRepositories.sort(Comparator.comparing(MergedRepository::getOrder));
        return mergedRepositories;
    }

}
