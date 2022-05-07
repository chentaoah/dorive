package com.gitee.spring.domain.core.repository;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.Assert;
import com.gitee.spring.domain.core.api.EntityMapper;
import com.gitee.spring.domain.core.api.EntityProperty;
import com.gitee.spring.domain.core.entity.BindingDefinition;
import com.gitee.spring.domain.core.entity.BoundedContext;
import com.gitee.spring.domain.core.entity.EntityDefinition;
import com.gitee.spring.domain.core.entity.EntityPropertyChain;
import com.gitee.spring.domain.core.utils.ReflectUtils;
import org.springframework.core.annotation.AnnotationAttributes;

import java.util.Collection;
import java.util.List;

public abstract class AbstractGenericRepository<E, PK> extends AbstractDelegateRepository<E, PK> {

    @Override
    @SuppressWarnings("unchecked")
    public E selectByPrimaryKey(BoundedContext boundedContext, PK primaryKey) {
        Object rootEntity;
        if (rootRepository != null) {
            rootEntity = rootRepository.selectByPrimaryKey(boundedContext, primaryKey);
        } else {
            rootEntity = ReflectUtils.newInstance(entityCtor, null);
        }
        if (rootEntity != null) {
            handleRootEntity(boundedContext, rootEntity);
        }
        return (E) rootEntity;
    }

    protected void handleRootEntity(BoundedContext boundedContext, Object rootEntity) {
        List<ConfiguredRepository> configuredRepositories = classSubRepositoriesMap.get(rootEntity.getClass());
        if (configuredRepositories != null && !configuredRepositories.isEmpty()) {
            for (ConfiguredRepository configuredRepository : configuredRepositories) {
                EntityPropertyChain entityPropertyChain = configuredRepository.getEntityPropertyChain();
                EntityProperty lastEntityProperty = entityPropertyChain.getLastEntityPropertyChain();
                Object lastEntity = lastEntityProperty == null ? rootEntity : lastEntityProperty.getValue(rootEntity);
                if (lastEntity != null && isMatchScenes(configuredRepository, boundedContext)) {
                    Object example = newExampleByContext(configuredRepository, boundedContext, rootEntity);
                    List<?> entities = configuredRepository.selectByExample(boundedContext, example);
                    Object entity = convertManyToOneEntity(configuredRepository, entities);
                    if (entity != null) {
                        EntityProperty entityProperty = entityPropertyChain.getEntityProperty();
                        entityProperty.setValue(lastEntity, entity);
                    }
                }
            }
        }
    }

    protected boolean isMatchScenes(ConfiguredRepository configuredRepository, BoundedContext boundedContext) {
        EntityDefinition entityDefinition = configuredRepository.getEntityDefinition();
        AnnotationAttributes attributes = entityDefinition.getAttributes();
        String[] sceneAttribute = attributes.getStringArray(SCENE_ATTRIBUTE);
        if (sceneAttribute.length == 0) {
            return true;
        }
        for (String scene : sceneAttribute) {
            if (boundedContext.containsKey(scene)) {
                return true;
            }
        }
        return false;
    }

    protected Object newExampleByContext(ConfiguredRepository configuredRepository, BoundedContext boundedContext, Object rootEntity) {
        EntityDefinition entityDefinition = configuredRepository.getEntityDefinition();
        EntityMapper entityMapper = configuredRepository.getEntityMapper();
        Object example = entityMapper.newExample(boundedContext);
        for (BindingDefinition bindingDefinition : entityDefinition.getBindingDefinitions()) {
            Object boundValue = getBoundValue(bindingDefinition, boundedContext, rootEntity);
            if (boundValue != null) {
                AnnotationAttributes bindingAttributes = bindingDefinition.getAttributes();
                String fieldAttribute = bindingAttributes.getString(FIELD_ATTRIBUTE);
                entityMapper.addToExample(example, fieldAttribute, boundValue);
            }
        }
        return example;
    }

    protected Object getBoundValue(BindingDefinition bindingDefinition, BoundedContext boundedContext, Object rootEntity) {
        Object boundValue;
        if (bindingDefinition.isFromContext()) {
            AnnotationAttributes bindingAttributes = bindingDefinition.getAttributes();
            String bindAttribute = bindingAttributes.getString(BIND_ATTRIBUTE);
            boundValue = boundedContext.get(bindAttribute);
        } else {
            EntityPropertyChain boundEntityPropertyChain = bindingDefinition.getBoundEntityPropertyChain();
            boundValue = boundEntityPropertyChain.getValue(rootEntity);
        }
        return boundValue;
    }

    protected Object convertManyToOneEntity(ConfiguredRepository configuredRepository, List<?> entities) {
        EntityDefinition entityDefinition = configuredRepository.getEntityDefinition();
        if (entityDefinition.isCollection()) {
            return entities;
        } else if (!entities.isEmpty()) {
            return entities.get(0);
        }
        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<E> selectByExample(BoundedContext boundedContext, Object example) {
        Assert.notNull(rootRepository, "Aggregation root is not annotated by @Entity, please use the [findByPrimaryKey] method.");
        List<?> entities = rootRepository.selectByExample(boundedContext, example);
        entities.forEach(entity -> handleRootEntity(boundedContext, entity));
        return (List<E>) entities;
    }

    @Override
    public <T> T selectPageByExample(BoundedContext boundedContext, Object example, Object page) {
        Assert.notNull(rootRepository, "Aggregation root is not annotated by @Entity, please use the [findByPrimaryKey] method.");
        T dataPage = rootRepository.selectPageByExample(boundedContext, example, page);
        EntityMapper entityMapper = rootRepository.getEntityMapper();
        List<?> entities = entityMapper.getDataFromPage(dataPage);
        entities.forEach(entity -> handleRootEntity(boundedContext, entity));
        return dataPage;
    }

    @Override
    public int insert(BoundedContext boundedContext, E entity) {
        Assert.notNull(entity, "The entity cannot be null!");
        int count = 0;
        List<ConfiguredRepository> orderedRepositories = classOrderedRepositoriesMap.get(entity.getClass());
        for (ConfiguredRepository configuredRepository : orderedRepositories) {
            EntityPropertyChain entityPropertyChain = configuredRepository.getEntityPropertyChain();
            Object targetEntity = entityPropertyChain == null ? entity : entityPropertyChain.getValue(entity);
            if (targetEntity != null && isMatchScenes(configuredRepository, boundedContext)) {
                if (targetEntity instanceof Collection) {
                    for (Object eachEntity : (Collection<?>) targetEntity) {
                        setBoundValueByContext(configuredRepository, boundedContext, entity, eachEntity);
                        count += configuredRepository.insert(boundedContext, eachEntity);
                    }
                } else {
                    setBoundValueByContext(configuredRepository, boundedContext, entity, targetEntity);
                    count += configuredRepository.insert(boundedContext, targetEntity);
                    Object primaryKey = BeanUtil.getFieldValue(targetEntity, "id");
                    setBoundIdForBoundEntity(configuredRepository, entity, primaryKey);
                }
            }
        }
        return count;
    }

    protected void setBoundValueByContext(ConfiguredRepository configuredRepository, BoundedContext boundedContext, Object rootEntity, Object entity) {
        EntityDefinition entityDefinition = configuredRepository.getEntityDefinition();
        for (BindingDefinition bindingDefinition : entityDefinition.getBindingDefinitions()) {
            if (!bindingDefinition.isBindId()) {
                Object boundValue = getBoundValue(bindingDefinition, boundedContext, rootEntity);
                if (boundValue != null) {
                    AnnotationAttributes bindingAttributes = bindingDefinition.getAttributes();
                    String fieldAttribute = bindingAttributes.getString(FIELD_ATTRIBUTE);
                    BeanUtil.setFieldValue(entity, fieldAttribute, boundValue);
                }
            }
        }
    }

    protected void setBoundIdForBoundEntity(ConfiguredRepository configuredRepository, Object rootEntity, Object primaryKey) {
        EntityDefinition entityDefinition = configuredRepository.getEntityDefinition();
        BindingDefinition boundIdBindingDefinition = entityDefinition.getBoundIdBindingDefinition();
        if (boundIdBindingDefinition != null && primaryKey != null) {
            EntityPropertyChain boundEntityPropertyChain = boundIdBindingDefinition.getBoundEntityPropertyChain();
            boundEntityPropertyChain.setValue(rootEntity, primaryKey);
        }
    }

    @Override
    public int update(BoundedContext boundedContext, E entity) {
        Assert.notNull(entity, "The entity cannot be null!");
        int count = 0;
        List<ConfiguredRepository> orderedRepositories = classOrderedRepositoriesMap.get(entity.getClass());
        for (ConfiguredRepository configuredRepository : orderedRepositories) {
            EntityPropertyChain entityPropertyChain = configuredRepository.getEntityPropertyChain();
            Object targetEntity = entityPropertyChain == null ? entity : entityPropertyChain.getValue(entity);
            if (targetEntity != null && isMatchScenes(configuredRepository, boundedContext)) {
                if (targetEntity instanceof Collection) {
                    for (Object eachEntity : (Collection<?>) targetEntity) {
                        count += configuredRepository.update(boundedContext, eachEntity);
                    }
                } else {
                    count += configuredRepository.update(boundedContext, targetEntity);
                }
            }
        }
        return count;
    }

    @Override
    public int updateByExample(E entity, Object example) {
        return rootRepository.updateByExample(entity, example);
    }

    @Override
    public int delete(BoundedContext boundedContext, E entity) {
        Assert.notNull(entity, "The entity cannot be null!");
        int count = 0;
        List<ConfiguredRepository> orderedRepositories = classOrderedRepositoriesMap.get(entity.getClass());
        for (ConfiguredRepository configuredRepository : orderedRepositories) {
            EntityPropertyChain entityPropertyChain = configuredRepository.getEntityPropertyChain();
            Object targetEntity = entityPropertyChain == null ? entity : entityPropertyChain.getValue(entity);
            if (targetEntity != null && isMatchScenes(configuredRepository, boundedContext)) {
                if (targetEntity instanceof Collection) {
                    for (Object eachEntity : (Collection<?>) targetEntity) {
                        count += configuredRepository.delete(boundedContext, eachEntity);
                    }
                } else {
                    count += configuredRepository.delete(boundedContext, targetEntity);
                }
            }
        }
        return count;
    }

    @Override
    public int deleteByPrimaryKey(PK primaryKey) {
        BoundedContext boundedContext = new BoundedContext();
        E entity = selectByPrimaryKey(boundedContext, primaryKey);
        return delete(new BoundedContext(), entity);
    }

    @Override
    public int deleteByExample(Object example) {
        return rootRepository.deleteByExample(example);
    }

}
