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
import com.gitee.spring.domain.coating.property.DefaultCoatingAssembler;
import com.gitee.spring.domain.coating.entity.PropertyDefinition;
import com.gitee.spring.domain.coating.utils.ResourceUtils;
import com.gitee.spring.domain.core.api.Constants;
import com.gitee.spring.domain.core.entity.EntityDefinition;
import com.gitee.spring.domain.core.entity.EntityPropertyChain;
import com.gitee.spring.domain.coating.entity.RepositoryLocation;
import com.gitee.spring.domain.core.repository.AbstractDelegateRepository;
import com.gitee.spring.domain.core.repository.ConfiguredRepository;
import com.gitee.spring.domain.event.repository.AbstractEventRepository;
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
public abstract class AbstractCoatingRepository<E, PK> extends AbstractEventRepository<E, PK> {

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
                    boolean isBound = false;

                    if (attributes != null) {
                        locationAttribute = attributes.getString(Constants.LOCATION_ATTRIBUTE);
                        aliasAttribute = attributes.getString(Constants.ALIAS_ATTRIBUTE);
                        isBound = locationAttribute.startsWith("/");
                    }
                    if (StringUtils.isBlank(aliasAttribute)) {
                        aliasAttribute = fieldName;
                    }

                    EntityPropertyChain entityPropertyChain = fieldEntityPropertyChainMap.get(fieldName);

                    PropertyDefinition propertyDefinition = new PropertyDefinition(declaredField, fieldClass, isCollection, genericFieldClass, fieldName,
                            attributes, locationAttribute, aliasAttribute, isBound, entityPropertyChain);

                    allPropertyDefinitionMap.put(fieldName, propertyDefinition);

                    if (entityPropertyChain != null && fieldClass == entityPropertyChain.getEntityClass()) {
                        availablePropertyDefinitions.add(propertyDefinition);
                    }

                    if (isBound) {
                        List<PropertyDefinition> propertyDefinitions = locationPropertyDefinitionsMap.computeIfAbsent(locationAttribute, key -> new ArrayList<>());
                        propertyDefinitions.add(propertyDefinition);
                    } else {
                        fieldPropertyDefinitionMap.put(fieldName, propertyDefinition);
                    }
                });

                Map<String, RepositoryLocation> repositoryLocationMap = new LinkedHashMap<>();
                collectRepositoryLocationMap(repositoryLocationMap, new ArrayList<>(), null, this,
                        locationPropertyDefinitionsMap, fieldPropertyDefinitionMap);

                List<RepositoryLocation> repositoryLocations = new ArrayList<>(repositoryLocationMap.values());
                checkFieldNames(coatingClass, allPropertyDefinitionMap.keySet(), repositoryLocations);
                Collections.reverse(repositoryLocations);

                AnnotationAttributes attributes = AnnotatedElementUtils.getMergedAnnotationAttributes(coatingClass, Coating.class);
                String name = null;
                if (attributes != null) {
                    name = attributes.getString(Constants.NAME_ATTRIBUTE);
                }
                if (StringUtils.isBlank(name)) {
                    name = StrUtil.lowerFirst(coatingClass.getSimpleName());
                }

                CoatingDefinition coatingDefinition = new CoatingDefinition(entityClass, coatingClass, attributes, name, allPropertyDefinitionMap);
                CoatingAssembler coatingAssembler = new DefaultCoatingAssembler(coatingDefinition, availablePropertyDefinitions, repositoryLocations);

                classCoatingAssemblerMap.put(coatingClass, coatingAssembler);
                Assert.isTrue(!nameCoatingAssemblerMap.containsKey(name), "The same coating name cannot exist!");
                nameCoatingAssemblerMap.putIfAbsent(name, coatingAssembler);
            }
        }
    }

    protected void collectRepositoryLocationMap(Map<String, RepositoryLocation> repositoryLocationMap,
                                                List<String> multiAccessPath,
                                                ConfiguredRepository parentConfiguredRepository,
                                                AbstractDelegateRepository<?, ?> abstractDelegateRepository,
                                                Map<String, List<PropertyDefinition>> locationPropertyDefinitionsMap,
                                                Map<String, PropertyDefinition> fieldPropertyDefinitionMap) {

        String parentAccessPath = multiAccessPath.size() > 1 ? StrUtil.join("", multiAccessPath.subList(0, multiAccessPath.size() - 1)) : "";
        String prefixAccessPath = StrUtil.join("", multiAccessPath);

        if (rootRepository != null) {
            EntityDefinition entityDefinition = rootRepository.getEntityDefinition();
            String absoluteAccessPath = prefixAccessPath + entityDefinition.getAccessPath();
            boolean forwardParent = entityDefinition.isRoot() && parentConfiguredRepository != null;

            if (locationPropertyDefinitionsMap.containsKey(absoluteAccessPath)) {
                List<PropertyDefinition> propertyDefinitions = locationPropertyDefinitionsMap.get(absoluteAccessPath);
                RepositoryLocation repositoryLocation = new RepositoryLocation(multiAccessPath, parentAccessPath, prefixAccessPath, absoluteAccessPath,
                        forwardParent, parentConfiguredRepository, abstractDelegateRepository, rootRepository, propertyDefinitions);
                repositoryLocationMap.put(absoluteAccessPath, repositoryLocation);
            }
        }

        for (EntityPropertyChain entityPropertyChain : entityPropertyChainMap.values()) {
            String accessPath = entityPropertyChain.getAccessPath();
            ConfiguredRepository configuredRepository = configuredRepositoryMap.get(accessPath);
            if (configuredRepository != null) {
                EntityDefinition entityDefinition = configuredRepository.getEntityDefinition();
                String absoluteAccessPath = prefixAccessPath + entityDefinition.getAccessPath();
                boolean forwardParent = entityDefinition.isRoot() && parentConfiguredRepository != null;

                if (locationPropertyDefinitionsMap.containsKey(absoluteAccessPath)) {
                    List<PropertyDefinition> propertyDefinitions = locationPropertyDefinitionsMap.get(absoluteAccessPath);
                    RepositoryLocation repositoryLocation = new RepositoryLocation(multiAccessPath, parentAccessPath, prefixAccessPath, absoluteAccessPath,
                            forwardParent, parentConfiguredRepository, abstractDelegateRepository, configuredRepository, propertyDefinitions);
                    repositoryLocationMap.put(absoluteAccessPath, repositoryLocation);
                }
            }

            String fieldName = entityPropertyChain.getFieldName();
            if (fieldPropertyDefinitionMap.containsKey(fieldName) || entityPropertyChain.isBoundProperty()) {
                String belongAccessPath = getBelongAccessPath(accessPath);
                ConfiguredRepository belongConfiguredRepository = configuredRepositoryMap.get(belongAccessPath);
                if (belongConfiguredRepository != null) {
                    EntityDefinition entityDefinition = belongConfiguredRepository.getEntityDefinition();
                    String absoluteAccessPath = prefixAccessPath + entityDefinition.getAccessPath();
                    boolean forwardParent = entityDefinition.isRoot() && parentConfiguredRepository != null;

                    if (!repositoryLocationMap.containsKey(absoluteAccessPath)) {
                        RepositoryLocation repositoryLocation = new RepositoryLocation(multiAccessPath, parentAccessPath, prefixAccessPath, absoluteAccessPath,
                                forwardParent, parentConfiguredRepository, abstractDelegateRepository, belongConfiguredRepository, new ArrayList<>());
                        repositoryLocationMap.put(absoluteAccessPath, repositoryLocation);
                    }

                    PropertyDefinition propertyDefinition = fieldPropertyDefinitionMap.get(fieldName);
                    if (propertyDefinition != null) {
                        RepositoryLocation repositoryLocation = repositoryLocationMap.get(absoluteAccessPath);
                        List<PropertyDefinition> propertyDefinitions = repositoryLocation.getCollectedPropertyDefinitions();
                        propertyDefinitions.add(propertyDefinition);
                    }
                }
            }
        }

        for (ConfiguredRepository configuredRepository : delegateConfiguredRepositories) {
            multiAccessPath = new ArrayList<>(multiAccessPath);
            EntityDefinition entityDefinition = configuredRepository.getEntityDefinition();
            multiAccessPath.add(entityDefinition.getAccessPath());
            AbstractDelegateRepository<?, ?> delegateRepository = (AbstractDelegateRepository<?, ?>) configuredRepository.getRepository();
            collectRepositoryLocationMap(repositoryLocationMap, multiAccessPath, configuredRepository, delegateRepository,
                    locationPropertyDefinitionsMap, fieldPropertyDefinitionMap);
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
