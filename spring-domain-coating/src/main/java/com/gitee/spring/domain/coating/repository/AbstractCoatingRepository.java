package com.gitee.spring.domain.coating.repository;

import cn.hutool.core.lang.Assert;
import com.gitee.spring.domain.coating.annotation.Coating;
import com.gitee.spring.domain.coating.annotation.CoatingScan;
import com.gitee.spring.domain.coating.annotation.IgnoreProperty;
import com.gitee.spring.domain.coating.annotation.Property;
import com.gitee.spring.domain.coating.api.CoatingAssembler;
import com.gitee.spring.domain.coating.api.CoatingRepository;
import com.gitee.spring.domain.coating.api.CustomAssembler;
import com.gitee.spring.domain.coating.entity.CoatingDefinition;
import com.gitee.spring.domain.coating.entity.PropertyDefinition;
import com.gitee.spring.domain.coating.entity.RepositoryDefinition;
import com.gitee.spring.domain.coating.entity.RepositoryLocation;
import com.gitee.spring.domain.coating.impl.DefaultCoatingAssembler;
import com.gitee.spring.domain.coating.utils.ResourceUtils;
import com.gitee.spring.domain.core.constants.Attribute;
import com.gitee.spring.domain.core.entity.BoundedContext;
import com.gitee.spring.domain.core.entity.EntityDefinition;
import com.gitee.spring.domain.core.entity.EntityExample;
import com.gitee.spring.domain.core.entity.EntityPropertyChain;
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
public abstract class AbstractCoatingRepository<E, PK> extends AbstractAwareRepository<E, PK> implements CoatingRepository<E, PK> {

    public static final Set<String> COATING_NAMES = new LinkedHashSet<>();
    protected Map<Class<?>, CoatingAssembler> classCoatingAssemblerMap = new ConcurrentHashMap<>();

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

                    Map<String, EntityPropertyChain> fieldEntityPropertyChainMap = entityPropertyResolver.getFieldEntityPropertyChainMap();
                    EntityPropertyChain entityPropertyChain = fieldEntityPropertyChainMap.get(fieldName);

                    PropertyDefinition propertyDefinition = new PropertyDefinition(
                            declaredField, fieldClass, isCollection, genericFieldClass, fieldName,
                            attributes, locationAttribute, aliasAttribute, operatorAttribute, isBoundLocation,
                            entityPropertyChain);

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

                List<RepositoryLocation> reversedRepositoryLocations = collectRepositoryLocations(locationPropertyDefinitionsMap, fieldPropertyDefinitionMap);
                Collections.reverse(reversedRepositoryLocations);
                checkFieldNames(coatingClass, allPropertyDefinitionMap.keySet(), reversedRepositoryLocations);

                AnnotationAttributes attributes = AnnotatedElementUtils.getMergedAnnotationAttributes(coatingClass, Coating.class);
                String name = null;
                if (attributes != null) {
                    name = attributes.getString(Attribute.NAME_ATTRIBUTE);
                }
                if (StringUtils.isBlank(name)) {
                    name = coatingClass.getSimpleName();
                }
                Assert.isTrue(!COATING_NAMES.contains(name), "The same coating name exists!");
                COATING_NAMES.add(name);

                CoatingDefinition coatingDefinition = new CoatingDefinition(entityClass, coatingClass, attributes, name, allPropertyDefinitionMap);
                CoatingAssembler coatingAssembler = new DefaultCoatingAssembler(coatingDefinition, availablePropertyDefinitions, reversedRepositoryLocations);
                classCoatingAssemblerMap.put(coatingClass, coatingAssembler);
            }
        }
    }

    protected List<RepositoryLocation> collectRepositoryLocations(Map<String, List<PropertyDefinition>> locationPropertyDefinitionsMap,
                                                                  Map<String, PropertyDefinition> fieldPropertyDefinitionMap) {
        List<RepositoryLocation> repositoryLocations = new ArrayList<>();

        for (RepositoryDefinition repositoryDefinition : repositoryDefinitionMap.values()) {
            String absoluteAccessPath = repositoryDefinition.getAbsoluteAccessPath();
            ConfiguredRepository configuredRepository = repositoryDefinition.getConfiguredRepository();
            EntityDefinition entityDefinition = configuredRepository.getEntityDefinition();
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
                repositoryLocations.add(repositoryLocation);
            }
        }

        return repositoryLocations;
    }

    protected void checkFieldNames(Class<?> coatingClass, Set<String> fieldNames, List<RepositoryLocation> repositoryLocations) {
        Set<String> remainFieldNames = new LinkedHashSet<>(fieldNames);
        for (RepositoryLocation repositoryLocation : repositoryLocations) {
            for (PropertyDefinition propertyDefinition : repositoryLocation.getCollectedPropertyDefinitions()) {
                remainFieldNames.remove(propertyDefinition.getFieldName());
            }
        }
        if (!remainFieldNames.isEmpty()) {
            String errorMessage = String.format("The field does not exist in the aggregate root! entity: %s, coating: %s, fieldNames: %s",
                    entityClass, coatingClass, remainFieldNames);
            throw new RuntimeException(errorMessage);
        }
    }

    @Override
    public List<E> selectByCoating(BoundedContext boundedContext, Object coatingObject) {
        EntityExample entityExample = buildExample(boundedContext, coatingObject);
        return selectByExample(boundedContext, entityExample);
    }

    @Override
    public <T> T selectPageByCoating(BoundedContext boundedContext, Object coatingObject, Object page) {
        EntityExample entityExample = buildExample(boundedContext, coatingObject);
        return selectPageByExample(boundedContext, entityExample, page);
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
