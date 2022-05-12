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
import org.springframework.core.annotation.AnnotationUtils;
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
        CoatingScan coatingScan = AnnotationUtils.getAnnotation(this.getClass(), CoatingScan.class);
        if (coatingScan != null) {
            resolveCoatingAssemblers(coatingScan.value());
        }
    }

    protected void resolveCoatingAssemblers(String... basePackages) throws Exception {
        for (String basePackage : basePackages) {
            List<Class<?>> classes = ResourceUtils.resolveClasses(basePackage);
            for (Class<?> coatingClass : classes) {
                Map<String, PropertyDefinition> propertyDefinitionMap = new LinkedHashMap<>();
                List<PropertyDefinition> availablePropertyDefinitions = new ArrayList<>();
                Map<String, PropertyDefinition> locationPropertyDefinitions = new LinkedHashMap<>();
                Map<String, PropertyDefinition> fieldPropertyDefinitions = new LinkedHashMap<>();

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

                    propertyDefinitionMap.put(fieldName, propertyDefinition);

                    if (entityPropertyChain != null && fieldClass == entityPropertyChain.getEntityClass()) {
                        availablePropertyDefinitions.add(propertyDefinition);
                    }

                    if (isBound) {
                        locationPropertyDefinitions.put(locationAttribute, propertyDefinition);
                    } else {
                        fieldPropertyDefinitions.put(fieldName, propertyDefinition);
                    }
                });

                List<RepositoryLocation> repositoryLocations = collectRepositoryLocations(locationPropertyDefinitions, fieldPropertyDefinitions);
                checkFieldNames(coatingClass, propertyDefinitionMap.keySet(), repositoryLocations);
                Collections.reverse(repositoryLocations);

                AnnotationAttributes attributes = AnnotatedElementUtils.getMergedAnnotationAttributes(coatingClass, Coating.class);
                String name = null;
                if (attributes != null) {
                    name = attributes.getString(Constants.NAME_ATTRIBUTE);
                }
                if (StringUtils.isBlank(name)) {
                    name = StrUtil.lowerFirst(coatingClass.getSimpleName());
                }

                CoatingDefinition coatingDefinition = new CoatingDefinition(entityClass, coatingClass, attributes, name, propertyDefinitionMap);
                CoatingAssembler coatingAssembler = new DefaultCoatingAssembler(coatingDefinition, availablePropertyDefinitions, repositoryLocations);

                classCoatingAssemblerMap.put(coatingClass, coatingAssembler);
                Assert.isTrue(!nameCoatingAssemblerMap.containsKey(name), "The same coating name cannot exist!");
                nameCoatingAssemblerMap.putIfAbsent(name, coatingAssembler);
            }
        }
    }

    protected List<RepositoryLocation> collectRepositoryLocations(Map<String, PropertyDefinition> locationPropertyDefinitions,
                                                                  Map<String, PropertyDefinition> fieldPropertyDefinitions) {
        List<RepositoryLocation> repositoryLocations = new ArrayList<>();
        collectRepositoryLocations(repositoryLocations, new ArrayList<>(), null, this, locationPropertyDefinitions, fieldPropertyDefinitions);
        return repositoryLocations;
    }

    protected void collectRepositoryLocations(List<RepositoryLocation> repositoryLocations,
                                              List<String> multiAccessPath,
                                              ConfiguredRepository parentConfiguredRepository,
                                              AbstractDelegateRepository<?, ?> abstractDelegateRepository,
                                              Map<String, PropertyDefinition> locationPropertyDefinitions,
                                              Map<String, PropertyDefinition> fieldPropertyDefinitions) {

        String prefixAccessPath = StrUtil.join("", multiAccessPath);
        String parentAccessPath = multiAccessPath.size() > 1 ? StrUtil.join("", multiAccessPath.subList(0, multiAccessPath.size() - 1)) : "";

        String rootAccessPath = prefixAccessPath + "/";
        if (locationPropertyDefinitions.containsKey(rootAccessPath)) {
            EntityDefinition entityDefinition = rootRepository.getEntityDefinition();
            boolean forwardParent = entityDefinition.isRoot() && parentConfiguredRepository != null;

            PropertyDefinition propertyDefinition = locationPropertyDefinitions.get(rootAccessPath);
            RepositoryLocation repositoryLocation = new RepositoryLocation(multiAccessPath, prefixAccessPath, parentAccessPath, rootAccessPath,
                    forwardParent, parentConfiguredRepository, abstractDelegateRepository, rootRepository, propertyDefinition);
            repositoryLocations.add(repositoryLocation);
        }

        for (EntityPropertyChain entityPropertyChain : entityPropertyChainMap.values()) {
            String accessPath = entityPropertyChain.getAccessPath();
            ConfiguredRepository configuredRepository = configuredRepositoryMap.get(accessPath);
            if (configuredRepository != null) {
                EntityDefinition entityDefinition = configuredRepository.getEntityDefinition();
                String absoluteAccessPath = prefixAccessPath + entityDefinition.getAccessPath();
                boolean forwardParent = entityDefinition.isRoot() && parentConfiguredRepository != null;

                if (locationPropertyDefinitions.containsKey(absoluteAccessPath)) {
                    PropertyDefinition propertyDefinition = locationPropertyDefinitions.get(absoluteAccessPath);
                    RepositoryLocation repositoryLocation = new RepositoryLocation(multiAccessPath, prefixAccessPath, parentAccessPath, absoluteAccessPath,
                            forwardParent, parentConfiguredRepository, abstractDelegateRepository, configuredRepository, propertyDefinition);
                    repositoryLocations.add(repositoryLocation);
                }
            }

            String fieldName = entityPropertyChain.getFieldName();
            if (fieldPropertyDefinitions.containsKey(fieldName) || entityPropertyChain.isBoundProperty()) {
                String belongAccessPath = getBelongAccessPath(accessPath);
                ConfiguredRepository belongConfiguredRepository = configuredRepositoryMap.get(belongAccessPath);
                if (belongConfiguredRepository != null) {
                    EntityDefinition entityDefinition = belongConfiguredRepository.getEntityDefinition();
                    String absoluteAccessPath = prefixAccessPath + entityDefinition.getAccessPath();
                    boolean forwardParent = entityDefinition.isRoot() && parentConfiguredRepository != null;

                    PropertyDefinition propertyDefinition = fieldPropertyDefinitions.get(fieldName);
                    RepositoryLocation repositoryLocation = new RepositoryLocation(multiAccessPath, prefixAccessPath, parentAccessPath, absoluteAccessPath,
                            forwardParent, parentConfiguredRepository, abstractDelegateRepository, belongConfiguredRepository, propertyDefinition);
                    repositoryLocations.add(repositoryLocation);
                }
            }
        }

        for (ConfiguredRepository configuredRepository : delegateConfiguredRepositories) {
            multiAccessPath = new ArrayList<>(multiAccessPath);
            EntityDefinition entityDefinition = configuredRepository.getEntityDefinition();
            multiAccessPath.add(entityDefinition.getAccessPath());
            AbstractDelegateRepository<?, ?> delegateRepository = (AbstractDelegateRepository<?, ?>) configuredRepository.getRepository();
            collectRepositoryLocations(repositoryLocations, multiAccessPath, configuredRepository, delegateRepository, locationPropertyDefinitions, fieldPropertyDefinitions);
        }
    }

    protected void checkFieldNames(Class<?> coatingClass, Set<String> fieldNames, List<RepositoryLocation> repositoryLocations) {
        Set<String> newFieldNames = new LinkedHashSet<>(fieldNames);
        for (RepositoryLocation repositoryLocation : repositoryLocations) {
            PropertyDefinition propertyDefinition = repositoryLocation.getPropertyDefinition();
            if (propertyDefinition != null) {
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
