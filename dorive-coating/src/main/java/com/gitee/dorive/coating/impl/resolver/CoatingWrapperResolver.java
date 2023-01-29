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
import com.gitee.dorive.coating.entity.definition.CoatingDefinition;
import com.gitee.dorive.coating.entity.definition.PropertyDefinition;
import com.gitee.dorive.coating.repository.AbstractCoatingRepository;
import com.gitee.dorive.coating.util.ResourceUtils;
import com.gitee.dorive.core.entity.EntityElement;
import com.gitee.dorive.core.entity.Property;
import com.gitee.dorive.core.repository.ConfiguredRepository;
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

    public CoatingWrapperResolver(AbstractCoatingRepository<?, ?> repository) {
        this.repository = repository;
    }

    public void resolveCoatingWrapperMap() throws Exception {
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
                Map<String, List<PropertyWrapper>> belongToPropertyWrappersMap = new LinkedHashMap<>();
                Map<String, PropertyWrapper> fieldPropertyWrapperMap = new LinkedHashMap<>();
                SpecificProperties specificProperties = new SpecificProperties();

                ReflectionUtils.doWithLocalFields(coatingClass, declaredField -> {
                    Property property = new Property(declaredField);
                    String fieldName = property.getFieldName();

                    PropertyDefinition propertyDefinition = PropertyDefinition.newPropertyDefinition(declaredField);
                    PropertyDefinition.renewPropertyDefinition(fieldName, propertyDefinition);
                    if (propertyDefinition.isIgnore()) {
                        return;
                    }

                    PropertyWrapper propertyWrapper = new PropertyWrapper(property, propertyDefinition);
                    if (specificProperties.addProperty(fieldName, propertyWrapper)) {
                        return;
                    }

                    fieldNames.add(fieldName);

                    String belongTo = propertyDefinition.getBelongTo();
                    if (StringUtils.isNotBlank(belongTo) && belongTo.startsWith("/")) {
                        List<PropertyWrapper> existPropertyWrappers = belongToPropertyWrappersMap.computeIfAbsent(belongTo, key -> new ArrayList<>());
                        existPropertyWrappers.add(propertyWrapper);
                    } else {
                        fieldPropertyWrapperMap.put(fieldName, propertyWrapper);
                    }
                });

                List<RepositoryWrapper> repositoryWrappers = collectRepositoryWrappers(belongToPropertyWrappersMap, fieldPropertyWrapperMap);
                checkFieldNames(coatingClass, fieldNames, repositoryWrappers);

                List<RepositoryWrapper> reversedRepositoryWrappers = new ArrayList<>(repositoryWrappers);
                Collections.reverse(reversedRepositoryWrappers);

                CoatingDefinition coatingDefinition = CoatingDefinition.newCoatingDefinition(coatingClass);
                CoatingWrapper coatingWrapper = new CoatingWrapper(coatingDefinition, repositoryWrappers, reversedRepositoryWrappers, specificProperties);
                coatingWrapperMap.put(coatingClass, coatingWrapper);
            }
        }
    }

    private List<RepositoryWrapper> collectRepositoryWrappers(Map<String, List<PropertyWrapper>> belongToPropertyWrappersMap,
                                                              Map<String, PropertyWrapper> fieldPropertyWrapperMap) {
        MergedRepositoryResolver mergedRepositoryResolver = repository.getMergedRepositoryResolver();
        Map<String, MergedRepository> mergedRepositoryMap = mergedRepositoryResolver.getMergedRepositoryMap();

        List<RepositoryWrapper> repositoryWrappers = new ArrayList<>();

        for (MergedRepository mergedRepository : mergedRepositoryMap.values()) {
            String absoluteAccessPath = mergedRepository.getAbsoluteAccessPath();
            ConfiguredRepository repository = mergedRepository.getConfiguredRepository();
            EntityElement entityElement = repository.getEntityElement();

            List<PropertyWrapper> propertyWrappers = new ArrayList<>();

            List<PropertyWrapper> belongToPropertyWrappers = belongToPropertyWrappersMap.get(absoluteAccessPath);
            if (belongToPropertyWrappers != null) {
                propertyWrappers.addAll(belongToPropertyWrappers);
            }

            for (String fieldName : entityElement.getProperties()) {
                PropertyWrapper propertyWrapper = fieldPropertyWrapperMap.get(fieldName);
                if (propertyWrapper != null) {
                    propertyWrappers.add(propertyWrapper);
                }
            }

            if (!propertyWrappers.isEmpty() || repository.isBoundEntity()) {
                RepositoryWrapper repositoryWrapper = new RepositoryWrapper(mergedRepository, propertyWrappers);
                repositoryWrappers.add(repositoryWrapper);
            }
        }

        return repositoryWrappers;
    }

    private void checkFieldNames(Class<?> coatingClass, Set<String> fieldNames, List<RepositoryWrapper> repositoryWrappers) {
        Set<String> remainFieldNames = new LinkedHashSet<>(fieldNames);
        for (RepositoryWrapper repositoryWrapper : repositoryWrappers) {
            for (PropertyWrapper propertyWrapper : repositoryWrapper.getCollectedPropertyWrappers()) {
                remainFieldNames.remove(propertyWrapper.getProperty().getFieldName());
            }
        }
        if (!remainFieldNames.isEmpty()) {
            String errorMessage = String.format("The field does not exist in the aggregate root! entity: %s, coating: %s, fieldNames: %s",
                    repository.getEntityClass(), coatingClass, remainFieldNames);
            throw new RuntimeException(errorMessage);
        }
    }

}
