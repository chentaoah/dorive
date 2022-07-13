package com.gitee.spring.domain.coating.repository;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.gitee.spring.domain.coating.annotation.Coating;
import com.gitee.spring.domain.coating.annotation.CoatingScan;
import com.gitee.spring.domain.coating.annotation.IgnoreProperty;
import com.gitee.spring.domain.coating.annotation.Property;
import com.gitee.spring.domain.coating.api.CoatingAssembler;
import com.gitee.spring.domain.coating.api.CustomAssembler;
import com.gitee.spring.domain.coating.entity.CoatingDefinition;
import com.gitee.spring.domain.coating.impl.DefaultCoatingAssembler;
import com.gitee.spring.domain.coating.entity.PropertyDefinition;
import com.gitee.spring.domain.coating.utils.ResourceUtils;
import com.gitee.spring.domain.core.constants.Attribute;
import com.gitee.spring.domain.core.entity.EntityDefinition;
import com.gitee.spring.domain.core.entity.EntityPropertyChain;
import com.gitee.spring.domain.coating.entity.RepositoryLocation;
import com.gitee.spring.domain.coating.entity.RepositoryDefinition;
import com.gitee.spring.domain.coating.entity.RepositoryGroup;
import com.gitee.spring.domain.core.repository.ConfiguredRepository;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Data
@EqualsAndHashCode(callSuper = false)
public abstract class AbstractCoatingRepository<E, PK> extends AbstractAwareRepository<E, PK> {

    protected Map<Class<?>, CoatingAssembler> classCoatingAssemblerMap = new ConcurrentHashMap<>();
    protected Map<String, CoatingAssembler> nameCoatingAssemblerMap = new ConcurrentHashMap<>();

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        CoatingScan coatingScan = AnnotatedElementUtils.getMergedAnnotation(this.getClass(), CoatingScan.class);
        if (coatingScan != null) {
            resolveCoatingAssemblers(coatingScan.value());
        }
    }

    protected void resolveCoatingAssemblers(String... basePackages) throws Exception {
        for (String basePackage : basePackages) {
            List<Class<?>> classes = ResourceUtils.resolveClasses(basePackage);
            for (Class<?> coatingClass : classes) {
                Map<String, PropertyDefinition> allPropertyDefinitionMap = new LinkedHashMap<>();
                List<PropertyDefinition> availablePropertyDefinitions = new ArrayList<>();
                Map<String, List<PropertyDefinition>> locationPropertyDefinitionsMap = new LinkedHashMap<>();
                Map<String, PropertyDefinition> fieldPropertyDefinitionMap = new LinkedHashMap<>();

                ReflectionUtils.doWithLocalFields(coatingClass, declaredField -> {
                    if (declaredField.isAnnotationPresent(IgnoreProperty.class)) return;

                    Class<?> fieldClass = declaredField.getType();
                    boolean isCollection = false;
                    Class<?> genericFieldClass = fieldClass;
                    String fieldName = declaredField.getName();

                    if (Collection.class.isAssignableFrom(fieldClass)) {
                        isCollection = true;
                        ParameterizedType parameterizedType = (ParameterizedType) declaredField.getGenericType();
                        Type actualTypeArgument = parameterizedType.getActualTypeArguments()[0];
                        genericFieldClass = (Class<?>) actualTypeArgument;
                    }

                    AnnotationAttributes attributes = AnnotatedElementUtils.getMergedAnnotationAttributes(declaredField, Property.class);
                    String locationAttribute = null;
                    String aliasAttribute = null;
                    String operatorAttribute = "=";
                    boolean isBoundLocation = false;

                    if (attributes != null) {
                        locationAttribute = attributes.getString(Attribute.LOCATION_ATTRIBUTE);
                        aliasAttribute = attributes.getString(Attribute.ALIAS_ATTRIBUTE);
                        operatorAttribute = attributes.getString(Attribute.OPERATOR_ATTRIBUTE);
                        isBoundLocation = locationAttribute.startsWith("/");
                    }
                    if (StringUtils.isBlank(aliasAttribute)) {
                        aliasAttribute = fieldName;
                    }

                    EntityPropertyChain entityPropertyChain = fieldEntityPropertyChainMap.get(fieldName);

                    PropertyDefinition propertyDefinition = new PropertyDefinition(
                            declaredField, fieldClass, isCollection, genericFieldClass, fieldName,
                            attributes, locationAttribute, aliasAttribute, operatorAttribute,
                            isBoundLocation, entityPropertyChain);

                    allPropertyDefinitionMap.put(fieldName, propertyDefinition);

                    if (entityPropertyChain != null && fieldClass == entityPropertyChain.getEntityClass()) {
                        availablePropertyDefinitions.add(propertyDefinition);
                    }

                    if (isBoundLocation) {
                        List<PropertyDefinition> propertyDefinitions = locationPropertyDefinitionsMap.computeIfAbsent(locationAttribute, key -> new ArrayList<>());
                        propertyDefinitions.add(propertyDefinition);
                    } else {
                        fieldPropertyDefinitionMap.put(fieldName, propertyDefinition);
                    }
                });

                List<RepositoryLocation> reversedRepositoryLocations = collectReversedRepositoryLocations(
                        locationPropertyDefinitionsMap, fieldPropertyDefinitionMap);
                checkFieldNames(coatingClass, allPropertyDefinitionMap.keySet(), reversedRepositoryLocations);

                AnnotationAttributes attributes = AnnotatedElementUtils.getMergedAnnotationAttributes(coatingClass, Coating.class);
                String name = null;
                if (attributes != null) {
                    name = attributes.getString(Attribute.NAME_ATTRIBUTE);
                }
                if (StringUtils.isBlank(name)) {
                    name = StrUtil.lowerFirst(coatingClass.getSimpleName());
                }

                CoatingDefinition coatingDefinition = new CoatingDefinition(entityClass, coatingClass, attributes, name, allPropertyDefinitionMap);
                CoatingAssembler coatingAssembler = new DefaultCoatingAssembler(
                        coatingDefinition, availablePropertyDefinitions, reversedRepositoryLocations);

                classCoatingAssemblerMap.put(coatingClass, coatingAssembler);
                Assert.isTrue(!nameCoatingAssemblerMap.containsKey(name), "The same coating name cannot exist!");
                nameCoatingAssemblerMap.putIfAbsent(name, coatingAssembler);
            }
        }
    }

    protected List<RepositoryLocation> collectReversedRepositoryLocations(Map<String, List<PropertyDefinition>> locationPropertyDefinitionsMap,
                                                                          Map<String, PropertyDefinition> fieldPropertyDefinitionMap) {
        List<RepositoryLocation> reversedRepositoryLocations = new ArrayList<>();

        collectReversedRepositoryLocations(locationPropertyDefinitionsMap, fieldPropertyDefinitionMap,
                reversedRepositoryLocations, rootRepositoryDefinition);

        for (RepositoryGroup repositoryGroup : reversedRepositoryGroups) {
            for (RepositoryDefinition repositoryDefinition : repositoryGroup.getRepositoryDefinitions()) {
                collectReversedRepositoryLocations(locationPropertyDefinitionsMap, fieldPropertyDefinitionMap,
                        reversedRepositoryLocations, repositoryDefinition);
            }
        }

        return reversedRepositoryLocations;
    }

    protected void collectReversedRepositoryLocations(Map<String, List<PropertyDefinition>> locationPropertyDefinitionsMap,
                                                      Map<String, PropertyDefinition> fieldPropertyDefinitionMap,
                                                      List<RepositoryLocation> reversedRepositoryLocations,
                                                      RepositoryDefinition repositoryDefinition) {
        String absoluteAccessPath = repositoryDefinition.getAbsoluteAccessPath();
        ConfiguredRepository definitionRepository = repositoryDefinition.getDefinitionRepository();
        EntityDefinition entityDefinition = definitionRepository.getEntityDefinition();

        List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
        List<PropertyDefinition> locationPropertyDefinitions = locationPropertyDefinitionsMap.get(absoluteAccessPath);
        if (locationPropertyDefinitions != null) {
            propertyDefinitions.addAll(locationPropertyDefinitions);
        }

        for (String fieldName : entityDefinition.getFieldNames()) {
            PropertyDefinition propertyDefinition = fieldPropertyDefinitionMap.get(fieldName);
            if (propertyDefinition != null) {
                propertyDefinitions.add(propertyDefinition);
            }
        }

        if (!propertyDefinitions.isEmpty() || entityDefinition.isBoundEntity()) {
            RepositoryLocation repositoryLocation = new RepositoryLocation(repositoryDefinition, propertyDefinitions);
            reversedRepositoryLocations.add(repositoryLocation);
        }
    }

    protected void checkFieldNames(Class<?> coatingClass, Set<String> fieldNames, List<RepositoryLocation> repositoryLocations) {
        Set<String> newFieldNames = new LinkedHashSet<>(fieldNames);
        for (RepositoryLocation repositoryLocation : repositoryLocations) {
            for (PropertyDefinition propertyDefinition : repositoryLocation.getCollectedPropertyDefinitions()) {
                newFieldNames.remove(propertyDefinition.getFieldName());
            }
        }
        if (!newFieldNames.isEmpty()) {
            String errorMessage = String.format("The field does not exist in the aggregate root! entity: %s, coating: %s, fieldNames: %s", entityClass, coatingClass, newFieldNames);
            throw new RuntimeException(errorMessage);
        }
    }

    public <T> T assemble(T coating, E entity) {
        CoatingAssembler coatingAssembler = classCoatingAssemblerMap.get(coating.getClass());
        Assert.notNull(coatingAssembler, "No coating assembler exists!");
        coatingAssembler.assemble(coating, entity);
        if (coating instanceof CustomAssembler) {
            ((CustomAssembler) coating).assembleBy(entity);
        }
        return coating;
    }

    public void disassemble(Object coating, E entity) {
        CoatingAssembler coatingAssembler = classCoatingAssemblerMap.get(coating.getClass());
        Assert.notNull(coatingAssembler, "No coating assembler exists!");
        coatingAssembler.disassemble(coating, entity);
        if (coating instanceof CustomAssembler) {
            ((CustomAssembler) coating).disassembleTo(entity);
        }
    }

}
