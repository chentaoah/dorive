package com.gitee.spring.domain.core.repository;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.Assert;
import com.gitee.spring.domain.core.api.EntityMapper;
import com.gitee.spring.domain.core.api.EntityProperty;
import com.gitee.spring.domain.core.api.RepositoryAware;
import com.gitee.spring.domain.core.entity.BindingDefinition;
import com.gitee.spring.domain.core.entity.BoundedContext;
import com.gitee.spring.domain.core.entity.EntityDefinition;
import com.gitee.spring.domain.core.entity.EntityPropertyChain;
import com.gitee.spring.domain.core.utils.ReflectUtils;
import org.springframework.core.annotation.AnnotationAttributes;

import java.util.Collection;
import java.util.List;

public abstract class AbstractGenericRepository<E, PK> extends AbstractContextRepository<E, PK> {

    @Override
    @SuppressWarnings("unchecked")
    public E selectByPrimaryKey(BoundedContext boundedContext, PK primaryKey) {
        Object rootEntity;
        if (rootRepository != null) {
            rootEntity = rootRepository.selectByPrimaryKey(boundedContext, primaryKey);
        } else {
            rootEntity = ReflectUtils.newInstance(constructor, null);
        }
        if (rootEntity != null) {
            handleRootEntity(boundedContext, rootEntity);
        }
        return (E) rootEntity;
    }

    protected void handleRootEntity(BoundedContext boundedContext, Object rootEntity) {
        bindRepository(rootEntity);
        for (ConfigurableRepository configurableRepository : subRepositories) {
            EntityPropertyChain entityPropertyChain = configurableRepository.getEntityPropertyChain();
            EntityProperty lastEntityProperty = entityPropertyChain.getLastEntityPropertyChain();
            Object lastEntity = lastEntityProperty == null ? rootEntity : lastEntityProperty.getValue(rootEntity);
            if (lastEntity != null && isMatchScenes(configurableRepository, boundedContext)) {
                Object example = newExampleByContext(configurableRepository, boundedContext, rootEntity);
                List<?> entities = configurableRepository.selectByExample(boundedContext, example);
                Object entity = convertManyToOneEntity(configurableRepository, entities);
                if (entity != null) {
                    EntityProperty entityProperty = entityPropertyChain.getEntityProperty();
                    entityProperty.setValue(lastEntity, entity);
                }
            }
        }
    }

    protected void bindRepository(Object entity) {
        if (entity instanceof RepositoryAware) {
            ((RepositoryAware) entity).setRepository(this);
        }
    }

    protected boolean isMatchScenes(ConfigurableRepository configurableRepository, BoundedContext boundedContext) {
        EntityDefinition entityDefinition = configurableRepository.getEntityDefinition();
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

    protected Object newExampleByContext(ConfigurableRepository configurableRepository, BoundedContext boundedContext, Object rootEntity) {
        EntityDefinition entityDefinition = configurableRepository.getEntityDefinition();
        Object example = newExample(entityDefinition, boundedContext);
        for (BindingDefinition bindingDefinition : entityDefinition.getBindingDefinitions()) {
            Object boundValue = getBoundValue(bindingDefinition, boundedContext, rootEntity);
            if (boundValue != null) {
                AnnotationAttributes bindingAttributes = bindingDefinition.getAttributes();
                String fieldAttribute = bindingAttributes.getString(FIELD_ATTRIBUTE);
                addToExample(entityDefinition, example, fieldAttribute, boundValue);
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

    protected Object convertManyToOneEntity(ConfigurableRepository configurableRepository, List<?> entities) {
        EntityDefinition entityDefinition = configurableRepository.getEntityDefinition();
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
        bindRepository(entity);
        int count = 0;
        for (ConfigurableRepository configurableRepository : orderedRepositories) {
            EntityPropertyChain entityPropertyChain = configurableRepository.getEntityPropertyChain();
            Object targetEntity = entityPropertyChain == null ? entity : entityPropertyChain.getValue(entity);
            if (targetEntity != null && isMatchScenes(configurableRepository, boundedContext)) {
                if (targetEntity instanceof Collection) {
                    for (Object eachEntity : (Collection<?>) targetEntity) {
                        setBoundValueByContext(configurableRepository, boundedContext, entity, eachEntity);
                        count += configurableRepository.insert(boundedContext, eachEntity);
                    }
                } else {
                    setBoundValueByContext(configurableRepository, boundedContext, entity, targetEntity);
                    count += configurableRepository.insert(boundedContext, targetEntity);
                    Object primaryKey = BeanUtil.getFieldValue(targetEntity, "id");
                    setBoundIdForBoundEntity(configurableRepository, entity, primaryKey);
                }
            }
        }
        return count;
    }

    protected void setBoundValueByContext(ConfigurableRepository configurableRepository, BoundedContext boundedContext, Object rootEntity, Object entity) {
        EntityDefinition entityDefinition = configurableRepository.getEntityDefinition();
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

    protected void setBoundIdForBoundEntity(ConfigurableRepository configurableRepository, Object rootEntity, Object primaryKey) {
        EntityDefinition entityDefinition = configurableRepository.getEntityDefinition();
        BindingDefinition boundIdBindingDefinition = entityDefinition.getBoundIdBindingDefinition();
        if (boundIdBindingDefinition != null && primaryKey != null) {
            EntityPropertyChain boundEntityPropertyChain = boundIdBindingDefinition.getBoundEntityPropertyChain();
            boundEntityPropertyChain.setValue(rootEntity, primaryKey);
        }
    }

    @Override
    public int update(BoundedContext boundedContext, E entity) {
        Assert.notNull(entity, "The entity cannot be null!");
        bindRepository(entity);
        int count = 0;
        for (ConfigurableRepository configurableRepository : orderedRepositories) {
            EntityPropertyChain entityPropertyChain = configurableRepository.getEntityPropertyChain();
            Object targetEntity = entityPropertyChain == null ? entity : entityPropertyChain.getValue(entity);
            if (targetEntity != null && isMatchScenes(configurableRepository, boundedContext)) {
                if (targetEntity instanceof Collection) {
                    for (Object eachEntity : (Collection<?>) targetEntity) {
                        count += configurableRepository.update(boundedContext, eachEntity);
                    }
                } else {
                    count += configurableRepository.update(boundedContext, targetEntity);
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
        bindRepository(entity);
        int count = 0;
        for (ConfigurableRepository configurableRepository : orderedRepositories) {
            EntityPropertyChain entityPropertyChain = configurableRepository.getEntityPropertyChain();
            Object targetEntity = entityPropertyChain == null ? entity : entityPropertyChain.getValue(entity);
            if (targetEntity != null && isMatchScenes(configurableRepository, boundedContext)) {
                if (targetEntity instanceof Collection) {
                    for (Object eachEntity : (Collection<?>) targetEntity) {
                        count += configurableRepository.delete(boundedContext, eachEntity);
                    }
                } else {
                    count += configurableRepository.delete(boundedContext, targetEntity);
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
