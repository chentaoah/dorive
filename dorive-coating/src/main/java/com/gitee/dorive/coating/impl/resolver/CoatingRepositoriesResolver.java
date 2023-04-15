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
public class CoatingRepositoriesResolver {

    private static Map<String, List<Class<?>>> scannedClasses = new ConcurrentHashMap<>();

    private AbstractCoatingRepository<?, ?> repository;
    private Map<Class<?>, CoatingRepositories> classCoatingRepositoriesMap = new ConcurrentHashMap<>();
    private Map<String, CoatingRepositories> nameCoatingRepositoriesMap = new ConcurrentHashMap<>();

    public CoatingRepositoriesResolver(AbstractCoatingRepository<?, ?> repository) throws Exception {
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

                SpecificProperties specificProperties = new SpecificProperties();
                Set<String> fieldNames = new LinkedHashSet<>();
                Map<String, List<Property>> belongToPropertyMap = new LinkedHashMap<>();
                Map<String, Property> fieldPropertyMap = new LinkedHashMap<>();

                ReflectionUtils.doWithLocalFields(coatingClass, declaredField -> {
                    Property property = new Property(declaredField);

                    PropertyDef propertyDef = property.getPropertyDef();
                    if (propertyDef.isIgnore()) {
                        return;
                    }

                    String fieldName = property.getName();
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

                List<PropertyRepository> propertyRepositories = collectRepositories(belongToPropertyMap, fieldPropertyMap);
                checkFieldNames(coatingClass, fieldNames, propertyRepositories);

                List<PropertyRepository> reversedPropertyRepositories = new ArrayList<>(propertyRepositories);
                Collections.reverse(reversedPropertyRepositories);

                CoatingDef coatingDef = CoatingDef.fromElement(coatingClass);
                CoatingRepositories coatingRepositories = new CoatingRepositories(coatingDef, propertyRepositories, reversedPropertyRepositories, specificProperties);
                classCoatingRepositoriesMap.put(coatingClass, coatingRepositories);
                nameCoatingRepositoriesMap.put(coatingClass.getName(), coatingRepositories);
            }
        }
    }

    private List<PropertyRepository> collectRepositories(Map<String, List<Property>> belongToPropertyMap, Map<String, Property> fieldPropertyMap) {
        MergedRepositoryResolver mergedRepositoryResolver = repository.getMergedRepositoryResolver();
        Map<String, MergedRepository> mergedRepositoryMap = mergedRepositoryResolver.getMergedRepositoryMap();

        List<PropertyRepository> propertyRepositories = new ArrayList<>();

        for (MergedRepository mergedRepository : mergedRepositoryMap.values()) {
            String absoluteAccessPath = mergedRepository.getAbsoluteAccessPath();
            CommonRepository repository = mergedRepository.getExecutedRepository();
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
                PropertyRepository propertyRepository = new PropertyRepository(mergedRepository, properties);
                propertyRepositories.add(propertyRepository);
            }
        }

        return propertyRepositories;
    }

    private void checkFieldNames(Class<?> coatingClass, Set<String> fieldNames, List<PropertyRepository> propertyRepositories) {
        Set<String> remainFieldNames = new LinkedHashSet<>(fieldNames);
        for (PropertyRepository propertyRepository : propertyRepositories) {
            for (Property property : propertyRepository.getCollectedProperties()) {
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
