package com.gitee.spring.domain.coating.repository;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.gitee.spring.domain.coating.annotation.Coating;
import com.gitee.spring.domain.coating.annotation.CoatingScan;
import com.gitee.spring.domain.coating.annotation.IgnoreProperty;
import com.gitee.spring.domain.coating.api.CoatingAssembler;
import com.gitee.spring.domain.coating.api.CustomAssembler;
import com.gitee.spring.domain.coating.entity.CoatingDefinition;
import com.gitee.spring.domain.coating.property.DefaultCoatingAssembler;
import com.gitee.spring.domain.coating.entity.PropertyDefinition;
import com.gitee.spring.domain.coating.utils.ResourceUtils;
import com.gitee.spring.domain.core.api.Constants;
import com.gitee.spring.domain.core.entity.EntityPropertyChain;
import com.gitee.spring.domain.core.entity.EntityPropertyLocation;
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

                    EntityPropertyChain entityPropertyChain = fieldEntityPropertyChainMap.get(fieldName);
                    PropertyDefinition propertyDefinition = new PropertyDefinition(declaredField, fieldClass, isCollection, genericFieldClass, fieldName, entityPropertyChain);
                    propertyDefinitionMap.put(fieldName, propertyDefinition);
                    if (entityPropertyChain != null && fieldClass == entityPropertyChain.getEntityClass()) {
                        availablePropertyDefinitions.add(propertyDefinition);
                    }
                });

                Set<String> fieldNames = propertyDefinitionMap.keySet();
                List<EntityPropertyLocation> entityPropertyLocations = collectEntityPropertyLocations(fieldNames);
                checkFieldNames(coatingClass, fieldNames, entityPropertyLocations);
                Collections.reverse(entityPropertyLocations);

                AnnotationAttributes attributes = AnnotatedElementUtils.getMergedAnnotationAttributes(coatingClass, Coating.class);
                String name = null;
                if (attributes != null) {
                    name = attributes.getString(Constants.NAME_ATTRIBUTE);
                }
                if (StringUtils.isBlank(name)) {
                    name = StrUtil.lowerFirst(coatingClass.getSimpleName());
                }

                CoatingDefinition coatingDefinition = new CoatingDefinition(entityClass, coatingClass, attributes, name, propertyDefinitionMap, entityPropertyLocations);
                CoatingAssembler coatingAssembler = new DefaultCoatingAssembler(coatingDefinition, availablePropertyDefinitions);

                classCoatingAssemblerMap.put(coatingClass, coatingAssembler);
                Assert.isTrue(!nameCoatingAssemblerMap.containsKey(name), "The same coating name cannot exist!");
                nameCoatingAssemblerMap.putIfAbsent(name, coatingAssembler);
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
