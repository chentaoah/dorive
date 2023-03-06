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

import com.gitee.dorive.coating.annotation.Coating;
import com.gitee.dorive.coating.entity.*;
import com.gitee.dorive.coating.entity.def.CoatingDef;
import com.gitee.dorive.coating.entity.def.PropertyDef;
import com.gitee.dorive.coating.repository.AbstractCoatingRepository;
import com.gitee.dorive.coating.util.ResourceUtils;
import com.gitee.dorive.api.entity.element.EntityEle;
import com.gitee.dorive.core.repository.CommonRepository;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

@Data
public class CoatingWrapperResolver {

    private static Map<String, List<Class<?>>> scannedClasses = new ConcurrentHashMap<>();

    private AbstractCoatingRepository<?, ?> repository;
    private Map<Class<?>, CoatingWrapper> coatingWrapperMap = new ConcurrentHashMap<>();
    private Map<String, CoatingWrapper> nameCoatingWrapperMap = new ConcurrentHashMap<>();

    public CoatingWrapperResolver(AbstractCoatingRepository<?, ?> repository) throws Exception {
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

                Set<String> fieldNames = new LinkedHashSet<>();
                Map<String, List<Property>> belongToPropertyMap = new LinkedHashMap<>();
                Map<String, Property> fieldPropertyMap = new LinkedHashMap<>();
                SpecificProperties specificProperties = new SpecificProperties();

                ReflectionUtils.doWithLocalFields(coatingClass, declaredField -> {
                    Property property = new Property(declaredField);
                    String fieldName = property.getName();

                    PropertyDef propertyDef = property.getPropertyDef();
                    PropertyDef.renew(fieldName, propertyDef);

                    if (propertyDef.isIgnore()) {
                        return;
                    }

                    if (specificProperties.addProperty(fieldName, property)) {
                        return;
                    }

                    fieldNames.add(fieldName);

                    String belongTo = propertyDef.getBelongTo();
                    if (StringUtils.isNotBlank(belongTo) && belongTo.startsWith("/")) {
                        List<Property> existProperties = belongToPropertyMap.computeIfAbsent(belongTo, key -> new ArrayList<>());
                        existProperties.add(property);
                    } else {
                        fieldPropertyMap.put(fieldName, property);
                    }
                });

                List<RepositoryWrapper> repositoryWrappers = collectRepositoryWrappers(belongToPropertyMap, fieldPropertyMap);
                checkFieldNames(coatingClass, fieldNames, repositoryWrappers);

                List<RepositoryWrapper> reversedRepositoryWrappers = new ArrayList<>(repositoryWrappers);
                Collections.reverse(reversedRepositoryWrappers);

                CoatingDef coatingDef = CoatingDef.fromElement(coatingClass);
                CoatingWrapper coatingWrapper = new CoatingWrapper(coatingDef, repositoryWrappers, reversedRepositoryWrappers, specificProperties);
                coatingWrapperMap.put(coatingClass, coatingWrapper);
                nameCoatingWrapperMap.put(coatingClass.getName(), coatingWrapper);
            }
        }
    }

    private List<RepositoryWrapper> collectRepositoryWrappers(Map<String, List<Property>> belongToPropertyMap,
                                                              Map<String, Property> fieldPropertyMap) {
        MergedRepositoryResolver mergedRepositoryResolver = repository.getMergedRepositoryResolver();
        Map<String, MergedRepository> mergedRepositoryMap = mergedRepositoryResolver.getMergedRepositoryMap();

        List<RepositoryWrapper> repositoryWrappers = new ArrayList<>();

        for (MergedRepository mergedRepository : mergedRepositoryMap.values()) {
            String absoluteAccessPath = mergedRepository.getAbsoluteAccessPath();
            CommonRepository repository = mergedRepository.getCommonRepository();
            EntityEle entityEle = repository.getEntityEle();

            List<Property> properties = new ArrayList<>();

            List<Property> belongToProperties = belongToPropertyMap.get(absoluteAccessPath);
            if (belongToProperties != null) {
                properties.addAll(belongToProperties);
            }

            Map<String, String> aliasMap = entityEle.getAliasMap();
            for (String fieldName : aliasMap.keySet()) {
                Property property = fieldPropertyMap.get(fieldName);
                if (property != null) {
                    properties.add(property);
                }
            }

            if (!properties.isEmpty() || repository.isBoundEntity()) {
                RepositoryWrapper repositoryWrapper = new RepositoryWrapper(mergedRepository, properties);
                repositoryWrappers.add(repositoryWrapper);
            }
        }

        return repositoryWrappers;
    }

    private void checkFieldNames(Class<?> coatingClass, Set<String> fieldNames, List<RepositoryWrapper> repositoryWrappers) {
        Set<String> remainFieldNames = new LinkedHashSet<>(fieldNames);
        for (RepositoryWrapper repositoryWrapper : repositoryWrappers) {
            for (Property property : repositoryWrapper.getCollectedProperties()) {
                remainFieldNames.remove(property.getName());
            }
        }
        if (!remainFieldNames.isEmpty()) {
            String errorMessage = String.format("The field does not exist in the aggregate root! entity: %s, coating: %s, fieldNames: %s",
                    repository.getEntityClass(), coatingClass, remainFieldNames);
            throw new RuntimeException(errorMessage);
        }
    }

}
