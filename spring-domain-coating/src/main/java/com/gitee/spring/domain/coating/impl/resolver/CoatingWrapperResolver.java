package com.gitee.spring.domain.coating.impl.resolver;

import com.gitee.spring.domain.coating.annotation.Coating;
import com.gitee.spring.domain.coating.entity.CoatingWrapper;
import com.gitee.spring.domain.coating.entity.PropertyWrapper;
import com.gitee.spring.domain.coating.entity.RepositoryWrapper;
import com.gitee.spring.domain.coating.entity.definition.CoatingDefinition;
import com.gitee.spring.domain.coating.entity.definition.PropertyDefinition;
import com.gitee.spring.domain.coating.repository.AbstractCoatingRepository;
import com.gitee.spring.domain.coating.util.ResourceUtils;
import com.gitee.spring.domain.core.entity.Property;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.ReflectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class CoatingWrapperResolver {

    private AbstractCoatingRepository<?, ?> repository;

    private Map<Class<?>, CoatingWrapper> coatingWrapperMap = new ConcurrentHashMap<>();

    public CoatingWrapperResolver(AbstractCoatingRepository<?, ?> repository) {
        this.repository = repository;
    }

    public void resolveCoatingWrapperMap(String... scanPackages) throws Exception {
        for (String scanPackage : scanPackages) {
            List<Class<?>> classes = ResourceUtils.resolveClasses(scanPackage);
            for (Class<?> coatingClass : classes) {

                if (!coatingClass.isAnnotationPresent(Coating.class)) {
                    continue;
                }

                Map<String, PropertyWrapper> allPropertyWrapperMap = new LinkedHashMap<>();
                Map<String, List<PropertyWrapper>> locationPropertyWrappersMap = new LinkedHashMap<>();
                Map<String, PropertyWrapper> fieldPropertyWrapperMap = new LinkedHashMap<>();

                ReflectionUtils.doWithLocalFields(coatingClass, declaredField -> {
                    Property property = new Property(declaredField);
                    String fieldName = property.getFieldName();

                    PropertyDefinition propertyDefinition = PropertyDefinition.newPropertyDefinition(declaredField);
                    if (propertyDefinition.isIgnore()) {
                        return;
                    }
                    if (StringUtils.isBlank(propertyDefinition.getAlias())) {
                        propertyDefinition.setAlias(fieldName);
                    }
                    if (StringUtils.isBlank(propertyDefinition.getOperator())) {
                        propertyDefinition.setOperator("=");
                    }

                    PropertyWrapper propertyWrapper = new PropertyWrapper(property, propertyDefinition);
                    allPropertyWrapperMap.put(fieldName, propertyWrapper);

                    String location = propertyDefinition.getLocation();
                    if (StringUtils.isNotBlank(location) && location.startsWith("/")) {
                        List<PropertyWrapper> propertyWrappers = locationPropertyWrappersMap.computeIfAbsent(location, key -> new ArrayList<>());
                        propertyWrappers.add(propertyWrapper);
                    } else {
                        fieldPropertyWrapperMap.put(fieldName, propertyWrapper);
                    }
                });

                RepoDefinitionResolver repoDefinitionResolver = repository.getRepoDefinitionResolver();
                List<RepositoryWrapper> repositoryWrappers = repoDefinitionResolver.collectRepositoryWrappers(locationPropertyWrappersMap, fieldPropertyWrapperMap);
                checkFieldNames(coatingClass, allPropertyWrapperMap.keySet(), repositoryWrappers);

                List<RepositoryWrapper> reversedRepositoryWrappers = new ArrayList<>(repositoryWrappers);
                Collections.reverse(reversedRepositoryWrappers);

                CoatingDefinition coatingDefinition = CoatingDefinition.newCoatingDefinition(coatingClass);
                CoatingWrapper coatingWrapper = new CoatingWrapper(coatingDefinition, repositoryWrappers, reversedRepositoryWrappers);
                coatingWrapperMap.put(coatingClass, coatingWrapper);
            }
        }
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
