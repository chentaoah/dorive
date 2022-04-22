package com.gitee.spring.domain.coating.repository;

import cn.hutool.core.lang.Assert;
import com.gitee.spring.domain.coating.annotation.CoatingScan;
import com.gitee.spring.domain.coating.annotation.IgnoreProperty;
import com.gitee.spring.domain.coating.api.CoatingAssembler;
import com.gitee.spring.domain.coating.api.CustomAssembler;
import com.gitee.spring.domain.coating.entity.CoatingDefinition;
import com.gitee.spring.domain.coating.property.DefaultCoatingAssembler;
import com.gitee.spring.domain.coating.entity.PropertyDefinition;
import com.gitee.spring.domain.coating.utils.ResourceUtils;
import com.gitee.spring.domain.core.entity.EntityPropertyChain;
import com.gitee.spring.domain.core.entity.EntityPropertyLocation;
import com.gitee.spring.domain.event.repository.AbstractEventRepository;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

public abstract class AbstractCoatingRepository<E, PK> extends AbstractEventRepository<E, PK> {

    protected Map<Class<?>, CoatingAssembler> classCoatingAssemblerMap = new LinkedHashMap<>();

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        CoatingScan coatingScan = AnnotationUtils.getAnnotation(this.getClass(), CoatingScan.class);
        if (coatingScan != null) {
            resolveCoatingDefinitions(coatingScan.value());
        }
    }

    protected void resolveCoatingDefinitions(String... basePackages) throws Exception {
        for (String basePackage : basePackages) {
            List<Class<?>> classes = ResourceUtils.resolveClasses(basePackage);
            for (Class<?> coatingClass : classes) {
                Map<String, PropertyDefinition> propertyDefinitionMap = new LinkedHashMap<>();
                ReflectionUtils.doWithLocalFields(coatingClass, field -> {
                    if (field.isAnnotationPresent(IgnoreProperty.class)) return;

                    Class<?> fieldClass = field.getType();
                    boolean isCollection = false;
                    Class<?> genericFieldClass = fieldClass;
                    String fieldName = field.getName();
                    if (Collection.class.isAssignableFrom(fieldClass)) {
                        isCollection = true;
                        ParameterizedType parameterizedType = (ParameterizedType) field.getGenericType();
                        Type actualTypeArgument = parameterizedType.getActualTypeArguments()[0];
                        genericFieldClass = (Class<?>) actualTypeArgument;
                    }

                    EntityPropertyChain entityPropertyChain = fieldEntityPropertyChainMap.get(fieldName);
                    PropertyDefinition propertyDefinition = new PropertyDefinition(field, fieldClass, isCollection, genericFieldClass, fieldName, entityPropertyChain);
                    propertyDefinitionMap.put(fieldName, propertyDefinition);
                });

                Set<String> fieldNames = propertyDefinitionMap.keySet();
                List<EntityPropertyLocation> entityPropertyLocations = collectEntityPropertyLocations(fieldNames);
                checkFieldNames(coatingClass, fieldNames, entityPropertyLocations);
                Collections.reverse(entityPropertyLocations);

                CoatingDefinition coatingDefinition = new CoatingDefinition(entityClass, coatingClass, propertyDefinitionMap, entityPropertyLocations);
                CoatingAssembler coatingAssembler = new DefaultCoatingAssembler(coatingDefinition);
                classCoatingAssemblerMap.put(coatingClass, coatingAssembler);
            }
        }
    }

    protected void checkFieldNames(Class<?> coatingClass, Set<String> fieldNames, List<EntityPropertyLocation> entityPropertyLocations) {
        Set<String> newFieldNames = new LinkedHashSet<>(fieldNames);
        for (EntityPropertyLocation entityPropertyLocation : entityPropertyLocations) {
            EntityPropertyChain entityPropertyChain = entityPropertyLocation.getEntityPropertyChain();
            newFieldNames.remove(entityPropertyChain.getFieldName());
        }
        if (!newFieldNames.isEmpty()) {
            String errorMessage = String.format("The field does not exist in the aggregate root! entity: %s, coating: %s, fieldNames: %s", entityClass, coatingClass, fieldNames);
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
