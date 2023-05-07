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

package com.gitee.dorive.coating.impl.resolver;

import cn.hutool.core.lang.Assert;
import com.gitee.dorive.api.entity.element.EntityEle;
import com.gitee.dorive.coating.annotation.Coating;
import com.gitee.dorive.coating.entity.CoatingField;
import com.gitee.dorive.coating.entity.CoatingType;
import com.gitee.dorive.coating.entity.MergedRepository;
import com.gitee.dorive.coating.entity.SpecificFields;
import com.gitee.dorive.coating.entity.def.CoatingDef;
import com.gitee.dorive.coating.entity.def.PropertyDef;
import com.gitee.dorive.coating.repository.AbstractCoatingRepository;
import com.gitee.dorive.coating.util.ResourceUtils;
import com.gitee.dorive.core.repository.CommonRepository;
import lombok.Data;
import org.springframework.core.annotation.AnnotationUtils;
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
public class CoatingTypeResolver {

    private static Map<String, List<Class<?>>> scannedClasses = new ConcurrentHashMap<>();

    private AbstractCoatingRepository<?, ?> repository;
    private Map<Class<?>, CoatingType> classCoatingTypeMap = new ConcurrentHashMap<>();
    private Map<String, CoatingType> nameCoatingTypeMap = new ConcurrentHashMap<>();

    public CoatingTypeResolver(AbstractCoatingRepository<?, ?> repository) throws Exception {
        this.repository = repository;
        resolve();
    }

    public void resolve() throws Exception {
        String[] scanPackages = repository.getScanPackages();
        String regex = repository.getRegex();
        Pattern pattern = Pattern.compile(regex);

        for (String scanPackage : scanPackages) {
            List<Class<?>> classes = scannedClasses.get(scanPackage);
            if (classes == null) {
                classes = ResourceUtils.resolveClasses(scanPackage);
                scannedClasses.put(scanPackage, classes);
            }

            for (Class<?> coatingClass : classes) {
                Coating coatingAnnotation = AnnotationUtils.getAnnotation(coatingClass, Coating.class);
                if (coatingAnnotation == null) {
                    continue;
                }
                String simpleName = coatingClass.getSimpleName();
                if (!pattern.matcher(simpleName).matches()) {
                    continue;
                }

                CoatingDef coatingDef = CoatingDef.fromElement(coatingClass);
                List<CoatingField> fields = new ArrayList<>();
                SpecificFields specificFields = new SpecificFields();

                ReflectionUtils.doWithLocalFields(coatingClass, declaredField -> {
                    CoatingField field = new CoatingField(declaredField);
                    if (field.isIgnore()) {
                        return;
                    }
                    if (specificFields.addProperty(field)) {
                        return;
                    }
                    fields.add(field);
                });

                List<MergedRepository> mergedRepositories = collectRepositories(fields);
                List<MergedRepository> reversedMergedRepositories = new ArrayList<>(mergedRepositories);
                Collections.reverse(reversedMergedRepositories);

                CoatingType coatingType = new CoatingType(coatingDef, fields, specificFields, mergedRepositories, reversedMergedRepositories);
                classCoatingTypeMap.put(coatingClass, coatingType);
                nameCoatingTypeMap.put(coatingClass.getName(), coatingType);
            }
        }
    }

    private List<MergedRepository> collectRepositories(List<CoatingField> fields) {
        MergedRepositoryResolver mergedRepositoryResolver = repository.getMergedRepositoryResolver();
        Map<String, MergedRepository> mergedRepositoryMap = mergedRepositoryResolver.getMergedRepositoryMap();

        Set<MergedRepository> mergedRepositorySet = new LinkedHashSet<>();

        for (CoatingField field : fields) {
            PropertyDef propertyDef = field.getPropertyDef();
            String belongTo = propertyDef.getBelongTo();
            MergedRepository mergedRepository = mergedRepositoryMap.get(belongTo);
            Assert.notNull(mergedRepository, "No merged repository found! belongTo: {}", belongTo);
            
            String fieldName = propertyDef.getField();
            CommonRepository repository = mergedRepository.getExecutedRepository();
            EntityEle entityEle = repository.getEntityEle();
            Map<String, String> aliasMap = entityEle.getAliasMap();
            Assert.isTrue(aliasMap.containsKey(fieldName), "The field does not exist within the entity! element: {}, field: {}",
                    entityEle.getElement(), fieldName);

            mergedRepositorySet.add(mergedRepository);
        }

        for (MergedRepository mergedRepository : mergedRepositoryMap.values()) {
            CommonRepository repository = mergedRepository.getExecutedRepository();
            if (repository.isBoundEntity()) {
                mergedRepositorySet.add(mergedRepository);
            }
        }

        List<MergedRepository> mergedRepositories = new ArrayList<>(mergedRepositorySet);
        mergedRepositories.sort(Comparator.comparing(MergedRepository::getSequence));
        return mergedRepositories;
    }

}
