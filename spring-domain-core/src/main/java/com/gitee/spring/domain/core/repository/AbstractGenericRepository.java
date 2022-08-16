package com.gitee.spring.domain.core.repository;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.Assert;
import com.gitee.spring.domain.core.api.*;
import com.gitee.spring.domain.core.constants.EntityState;
import com.gitee.spring.domain.core.entity.*;
import com.gitee.spring.domain.core.impl.EntityStateResolver;
import com.gitee.spring.domain.core.utils.StringUtils;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public abstract class AbstractGenericRepository<E, PK> extends AbstractDelegateRepository<E, PK> implements GenericRepository<E, PK> {

    protected EntityStateResolver entityStateResolver = new EntityStateResolver();

    @Override
    @SuppressWarnings("unchecked")
    public E selectByPrimaryKey(BoundedContext boundedContext, PK primaryKey) {
        Object rootEntity = rootRepository.selectByPrimaryKey(boundedContext, primaryKey);
        handleRootEntity(boundedContext, rootEntity);
        return (E) rootEntity;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<E> selectByExample(BoundedContext boundedContext, Object example) {
        List<Object> rootEntities = rootRepository.selectByExample(boundedContext, example);
        handleRootEntities(boundedContext, rootEntities);
        return (List<E>) rootEntities;
    }

    @Override
    public <T> T selectPageByExample(BoundedContext boundedContext, Object example, Object page) {
        T dataPage = rootRepository.selectPageByExample(boundedContext, example, page);
        EntityMapper entityMapper = rootRepository.getEntityMapper();
        List<Object> rootEntities = entityMapper.getDataFromPage(dataPage);
        handleRootEntities(boundedContext, rootEntities);
        return dataPage;
    }

    protected void handleRootEntities(BoundedContext boundedContext, List<Object> rootEntities) {
        rootEntities.forEach(rootEntity -> handleRootEntity(boundedContext, rootEntity));
    }

    protected void handleRootEntity(BoundedContext boundedContext, Object rootEntity) {
        if (rootEntity == null) return;
        AbstractDelegateRepository<?, ?> abstractDelegateRepository = adaptiveRepository(rootEntity);
        for (ConfiguredRepository configuredRepository : abstractDelegateRepository.getSubRepositories()) {
            EntityPropertyChain entityPropertyChain = configuredRepository.getEntityPropertyChain();
            EntityPropertyChain lastEntityPropertyChain = entityPropertyChain.getLastEntityPropertyChain();
            Object lastEntity = lastEntityPropertyChain == null ? rootEntity : lastEntityPropertyChain.getValue(rootEntity);
            if (lastEntity != null && isMatchScenes(boundedContext, configuredRepository)) {
                EntityExample entityExample = newExampleByContext(boundedContext, rootEntity, configuredRepository);
                if (!entityExample.isEmptyQuery() && entityExample.isDirtyQuery()) {
                    List<?> entities = configuredRepository.selectByExample(boundedContext, entityExample);
                    Object entity = convertManyToOneEntity(configuredRepository, entities);
                    if (entity != null) {
                        EntityProperty entityProperty = entityPropertyChain.getEntityProperty();
                        entityProperty.setValue(lastEntity, entity);
                    }
                }
            }
        }
    }

    protected boolean isMatchScenes(BoundedContext boundedContext, ConfiguredRepository configuredRepository) {
        EntityDefinition entityDefinition = configuredRepository.getEntityDefinition();
        Set<String> sceneAttribute = entityDefinition.getSceneAttribute();
        return isMatchScenes(boundedContext, sceneAttribute);
    }

    protected boolean isMatchScenes(BoundedContext boundedContext, Set<String> sceneAttribute) {
        if (sceneAttribute.isEmpty()) {
            return true;
        }
        for (String scene : sceneAttribute) {
            if (boundedContext.containsKey(scene)) {
                return true;
            }
        }
        return false;
    }

    protected EntityExample newExampleByContext(BoundedContext boundedContext, Object rootEntity, ConfiguredRepository configuredRepository) {
        EntityExample entityExample = new EntityExample();
        for (EntityBinder entityBinder : configuredRepository.getBoundEntityBinders()) {
            String columnName = entityBinder.getColumnName();
            Object queryParameter = entityBinder.getBoundValue(boundedContext, rootEntity);
            if (queryParameter != null) {
                entityExample.eq(columnName, queryParameter);
            } else {
                entityExample.setEmptyQuery(true);
                break;
            }
        }
        if (!entityExample.isEmptyQuery() && entityExample.isDirtyQuery()) {
            newCriterionByContext(boundedContext, rootEntity, configuredRepository, entityExample);
        }
        return entityExample;
    }

    protected void newCriterionByContext(BoundedContext boundedContext, Object rootEntity, ConfiguredRepository configuredRepository, EntityExample entityExample) {
        for (EntityBinder entityBinder : configuredRepository.getContextEntityBinders()) {
            String columnName = entityBinder.getColumnName();
            Object queryParameter = entityBinder.getBoundValue(boundedContext, rootEntity);
            if (queryParameter != null) {
                if (queryParameter instanceof String && StringUtils.isLike((String) queryParameter)) {
                    queryParameter = StringUtils.stripLike((String) queryParameter);
                    entityExample.like(columnName, queryParameter);
                } else {
                    entityExample.eq(columnName, queryParameter);
                }
            }
        }
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
    public int insert(BoundedContext boundedContext, E entity) {
        return operateEntityByState(boundedContext, entity, EntityState.INSERT);
    }

    @Override
    public int update(BoundedContext boundedContext, E entity) {
        return operateEntityByState(boundedContext, entity, EntityState.UPDATE);
    }

    protected int operateEntityByState(BoundedContext boundedContext, E entity, int expectedEntityState) {
        Assert.notNull(entity, "The entity cannot be null!");
        int totalCount = 0;
        AbstractDelegateRepository<?, ?> abstractDelegateRepository = adaptiveRepository(entity);
        for (ConfiguredRepository configuredRepository : abstractDelegateRepository.getOrderedRepositories()) {
            EntityPropertyChain entityPropertyChain = configuredRepository.getEntityPropertyChain();
            Object targetEntity = entityPropertyChain == null ? entity : entityPropertyChain.getValue(entity);
            if (targetEntity != null && isMatchScenes(boundedContext, configuredRepository)) {
                int contextEntityState = entityStateResolver.resolveEntityStateByContext(boundedContext, configuredRepository);
                if (targetEntity instanceof Collection) {
                    for (Object eachEntity : (Collection<?>) targetEntity) {
                        int entityState = entityStateResolver.resolveEntityState(expectedEntityState, contextEntityState, eachEntity);
                        if (entityState == EntityState.INSERT_OR_UPDATE) {
                            getBoundValueFromContext(boundedContext, entity, configuredRepository, eachEntity);
                            totalCount += configuredRepository.insertOrUpdate(boundedContext, eachEntity);

                        } else if (entityState == EntityState.INSERT) {
                            getBoundValueFromContext(boundedContext, entity, configuredRepository, eachEntity);
                            totalCount += configuredRepository.insert(boundedContext, eachEntity);

                        } else if (entityState == EntityState.UPDATE) {
                            totalCount += configuredRepository.update(boundedContext, eachEntity);

                        } else if (entityState == EntityState.DELETE) {
                            totalCount += configuredRepository.delete(boundedContext, eachEntity);
                        }
                    }
                } else {
                    int entityState = entityStateResolver.resolveEntityState(expectedEntityState, contextEntityState, targetEntity);
                    if (entityState == EntityState.INSERT_OR_UPDATE) {
                        getBoundValueFromContext(boundedContext, entity, configuredRepository, targetEntity);
                        totalCount += configuredRepository.insertOrUpdate(boundedContext, targetEntity);
                        setBoundIdForBoundEntity(boundedContext, entity, configuredRepository, targetEntity);

                    } else if (entityState == EntityState.INSERT) {
                        getBoundValueFromContext(boundedContext, entity, configuredRepository, targetEntity);
                        totalCount += configuredRepository.insert(boundedContext, targetEntity);
                        setBoundIdForBoundEntity(boundedContext, entity, configuredRepository, targetEntity);

                    } else if (entityState == EntityState.UPDATE) {
                        totalCount += configuredRepository.update(boundedContext, targetEntity);

                    } else if (entityState == EntityState.DELETE) {
                        totalCount += configuredRepository.delete(boundedContext, targetEntity);
                    }
                }
            }
        }
        return totalCount;
    }

    protected void getBoundValueFromContext(BoundedContext boundedContext, Object rootEntity, ConfiguredRepository configuredRepository, Object entity) {
        for (EntityBinder entityBinder : configuredRepository.getBoundValueEntityBinders()) {
            Object fieldValue = entityBinder.getFieldValue(boundedContext, entity);
            if (fieldValue == null) {
                Object boundValue = entityBinder.getBoundValue(boundedContext, rootEntity);
                if (boundValue != null) {
                    entityBinder.setFieldValue(boundedContext, entity, boundValue);
                }
            }
        }
    }

    protected void setBoundIdForBoundEntity(BoundedContext boundedContext, Object rootEntity, ConfiguredRepository configuredRepository, Object entity) {
        EntityBinder entityBinder = configuredRepository.getBoundIdEntityBinder();
        if (entityBinder != null) {
            Object primaryKey = BeanUtil.getFieldValue(entity, "id");
            if (primaryKey != null) {
                entityBinder.setBoundValue(boundedContext, rootEntity, primaryKey);
            }
        }
    }

    @Override
    public int updateByExample(BoundedContext boundedContext, Object entity, Object example) {
        Assert.notNull(entity, "The entity cannot be null!");
        int totalCount = 0;
        boolean ignoreRoot = boundedContext.containsKey("#ignoreRoot");
        if (ignoreRoot) {
            boundedContext.remove("#ignoreRoot");
        }
        if (!ignoreRoot && isMatchScenes(boundedContext, getRootRepository())) {
            totalCount += getRootRepository().updateByExample(boundedContext, entity, example);
        }
        for (ConfiguredRepository configuredRepository : getSubRepositories()) {
            if (isMatchScenes(boundedContext, configuredRepository)) {
                totalCount += configuredRepository.updateByExample(boundedContext, entity, example);
            }
        }
        return totalCount;
    }

    @Override
    public int insertOrUpdate(BoundedContext boundedContext, E entity) {
        return operateEntityByState(boundedContext, entity, EntityState.INSERT_OR_UPDATE);
    }

    @Override
    public int delete(BoundedContext boundedContext, E entity) {
        return operateEntityByState(boundedContext, entity, EntityState.DELETE);
    }

    @Override
    public int deleteByPrimaryKey(BoundedContext boundedContext, PK primaryKey) {
        E entity = selectByPrimaryKey(boundedContext, primaryKey);
        return delete(boundedContext, entity);
    }

    @Override
    public int deleteByExample(BoundedContext boundedContext, Object example) {
        int totalCount = 0;
        boolean ignoreRoot = boundedContext.containsKey("#ignoreRoot");
        if (ignoreRoot) {
            boundedContext.remove("#ignoreRoot");
        }
        if (!ignoreRoot && isMatchScenes(boundedContext, getRootRepository())) {
            totalCount += getRootRepository().deleteByExample(boundedContext, example);
        }
        for (ConfiguredRepository configuredRepository : getSubRepositories()) {
            if (isMatchScenes(boundedContext, configuredRepository)) {
                totalCount += configuredRepository.deleteByExample(boundedContext, example);
            }
        }
        return totalCount;
    }

    @Override
    public int insertList(BoundedContext boundedContext, List<E> entities) {
        return entities.stream().mapToInt(entity -> insert(boundedContext, entity)).sum();
    }

    @Override
    public int updateList(BoundedContext boundedContext, List<E> entities) {
        return entities.stream().mapToInt(entity -> update(boundedContext, entity)).sum();
    }

    @Override
    public int insertOrUpdateList(BoundedContext boundedContext, List<E> entities) {
        return entities.stream().mapToInt(entity -> insertOrUpdate(boundedContext, entity)).sum();
    }

    @Override
    public int deleteList(BoundedContext boundedContext, List<E> entities) {
        return entities.stream().mapToInt(entity -> delete(boundedContext, entity)).sum();
    }

}
