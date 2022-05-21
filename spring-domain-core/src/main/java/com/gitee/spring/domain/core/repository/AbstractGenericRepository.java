package com.gitee.spring.domain.core.repository;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.Assert;
import com.gitee.spring.domain.core.api.EntityCriterion;
import com.gitee.spring.domain.core.api.EntityMapper;
import com.gitee.spring.domain.core.api.EntityProperty;
import com.gitee.spring.domain.core.entity.*;

import java.util.Collection;
import java.util.List;

public abstract class AbstractGenericRepository<E, PK> extends AbstractDelegateRepository<E, PK> {

    @Override
    @SuppressWarnings("unchecked")
    public E selectByPrimaryKey(BoundedContext boundedContext, PK primaryKey) {
        Object rootEntity = rootRepository.selectByPrimaryKey(boundedContext, primaryKey);
        handleRootEntity(boundedContext, rootEntity);
        return (E) rootEntity;
    }

    protected void handleRootEntity(BoundedContext boundedContext, Object rootEntity) {
        if (rootEntity == null) return;
        AbstractDelegateRepository<?, ?> abstractDelegateRepository = adaptiveRepository(rootEntity);
        for (ConfiguredRepository configuredRepository : abstractDelegateRepository.getSubRepositories()) {
            EntityPropertyChain entityPropertyChain = configuredRepository.getEntityPropertyChain();
            EntityProperty lastEntityProperty = entityPropertyChain.getLastEntityPropertyChain();
            Object lastEntity = lastEntityProperty == null ? rootEntity : lastEntityProperty.getValue(rootEntity);
            if (lastEntity != null && isMatchScenes(configuredRepository, boundedContext)) {
                EntityExample entityExample = newExampleByContext(configuredRepository, boundedContext, rootEntity);
                if (entityExample.isDirtyQuery()) {
                    List<?> entities = configuredRepository.selectByExample(boundedContext, entityExample.buildExample());
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
        String[] sceneAttribute = entityDefinition.getSceneAttribute();
        if (sceneAttribute.length == 0) return true;
        for (String scene : sceneAttribute) {
            if (boundedContext.containsKey(scene)) {
                return true;
            }
        }
        return false;
    }

    protected EntityExample newExampleByContext(ConfiguredRepository configuredRepository, BoundedContext boundedContext, Object rootEntity) {
        EntityDefinition entityDefinition = configuredRepository.getEntityDefinition();
        EntityMapper entityMapper = configuredRepository.getEntityMapper();
        EntityExample entityExample = entityMapper.newExample(entityDefinition, boundedContext);
        for (BindingDefinition bindingDefinition : entityDefinition.getBindingDefinitions()) {
            Object boundValue = getBoundValue(bindingDefinition, boundedContext, rootEntity);
            if (boundValue != null) {
                String aliasAttribute = bindingDefinition.getAliasAttribute();
                EntityCriterion entityCriterion = entityMapper.newEqualsCriterion(aliasAttribute, boundValue);
                entityExample.addCriterion(entityCriterion);
            }
        }
        return entityExample;
    }

    protected Object getBoundValue(BindingDefinition bindingDefinition, BoundedContext boundedContext, Object rootEntity) {
        Object boundValue;
        if (bindingDefinition.isFromContext()) {
            String bindAttribute = bindingDefinition.getBindAttribute();
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
        List<?> entities = rootRepository.selectByExample(boundedContext, example);
        entities.forEach(entity -> handleRootEntity(boundedContext, entity));
        return (List<E>) entities;
    }

    @Override
    public <T> T selectPageByExample(BoundedContext boundedContext, Object example, Object page) {
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
        AbstractDelegateRepository<?, ?> abstractDelegateRepository = adaptiveRepository(entity);
        for (ConfiguredRepository configuredRepository : abstractDelegateRepository.getOrderedRepositories()) {
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
            if (!bindingDefinition.isBoundId()) {
                Object boundValue = getBoundValue(bindingDefinition, boundedContext, rootEntity);
                if (boundValue != null) {
                    String fieldAttribute = bindingDefinition.getFieldAttribute();
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
        AbstractDelegateRepository<?, ?> abstractDelegateRepository = adaptiveRepository(entity);
        for (ConfiguredRepository configuredRepository : abstractDelegateRepository.getOrderedRepositories()) {
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
        AbstractDelegateRepository<?, ?> abstractDelegateRepository = adaptiveRepository(entity);
        for (ConfiguredRepository configuredRepository : abstractDelegateRepository.getOrderedRepositories()) {
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
