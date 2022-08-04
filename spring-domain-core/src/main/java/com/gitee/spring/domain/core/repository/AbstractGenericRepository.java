package com.gitee.spring.domain.core.repository;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.Assert;
import com.gitee.spring.domain.core.api.EntityCriterion;
import com.gitee.spring.domain.core.api.EntityMapper;
import com.gitee.spring.domain.core.api.EntityProperty;
import com.gitee.spring.domain.core.api.GenericRepository;
import com.gitee.spring.domain.core.constants.Operator;
import com.gitee.spring.domain.core.entity.BindingDefinition;
import com.gitee.spring.domain.core.entity.BoundedContext;
import com.gitee.spring.domain.core.entity.EntityDefinition;
import com.gitee.spring.domain.core.entity.EntityExample;
import com.gitee.spring.domain.core.entity.EntityPropertyChain;
import com.gitee.spring.domain.core.utils.StringUtils;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public abstract class AbstractGenericRepository<E, PK> extends AbstractDelegateRepository<E, PK> implements GenericRepository<E, PK> {

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
        EntityDefinition entityDefinition = configuredRepository.getEntityDefinition();
        EntityMapper entityMapper = configuredRepository.getEntityMapper();
        EntityExample entityExample = entityMapper.newExample(boundedContext, entityDefinition);
        for (BindingDefinition bindingDefinition : entityDefinition.getBoundBindingDefinitions()) {
            EntityPropertyChain boundEntityPropertyChain = bindingDefinition.getBoundEntityPropertyChain();
            Object boundValue = boundEntityPropertyChain.getValue(rootEntity);
            if (boundValue != null) {
                String aliasAttribute = bindingDefinition.getAliasAttribute();
                EntityCriterion entityCriterion = entityMapper.newCriterion(aliasAttribute, Operator.EQ, boundValue);
                entityExample.addCriterion(entityCriterion);
            } else {
                entityExample.setEmptyQuery(true);
                break;
            }
        }
        if (!entityExample.isEmptyQuery() && entityExample.isDirtyQuery()) {
            newCriterionByContext(boundedContext, configuredRepository, entityExample);
        }
        return entityExample;
    }

    protected void newCriterionByContext(BoundedContext boundedContext, ConfiguredRepository configuredRepository, EntityExample entityExample) {
        EntityDefinition entityDefinition = configuredRepository.getEntityDefinition();
        EntityMapper entityMapper = configuredRepository.getEntityMapper();
        for (BindingDefinition bindingDefinition : entityDefinition.getContextBindingDefinitions()) {
            String bindAttribute = bindingDefinition.getBindAttribute();
            Object boundValue = boundedContext.get(bindAttribute);
            if (boundValue != null) {
                String aliasAttribute = bindingDefinition.getAliasAttribute();
                String operator = Operator.EQ;
                if (boundValue instanceof String && StringUtils.isLike((String) boundValue)) {
                    operator = Operator.LIKE;
                    boundValue = StringUtils.stripLike((String) boundValue);
                }
                EntityCriterion entityCriterion = entityMapper.newCriterion(aliasAttribute, operator, boundValue);
                entityExample.addCriterion(entityCriterion);
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

    @Override
    public int insert(BoundedContext boundedContext, E entity) {
        Assert.notNull(entity, "The entity cannot be null!");
        int totalCount = 0;
        AbstractDelegateRepository<?, ?> abstractDelegateRepository = adaptiveRepository(entity);
        for (ConfiguredRepository configuredRepository : abstractDelegateRepository.getOrderedRepositories()) {
            EntityPropertyChain entityPropertyChain = configuredRepository.getEntityPropertyChain();
            Object targetEntity = entityPropertyChain == null ? entity : entityPropertyChain.getValue(entity);
            if (targetEntity != null && isMatchScenes(boundedContext, configuredRepository)) {
                if (targetEntity instanceof Collection) {
                    for (Object eachEntity : (Collection<?>) targetEntity) {
                        setBoundValueByContext(boundedContext, entity, configuredRepository, eachEntity);
                        totalCount += configuredRepository.insert(boundedContext, eachEntity);
                    }
                } else {
                    setBoundValueByContext(boundedContext, entity, configuredRepository, targetEntity);
                    totalCount += configuredRepository.insert(boundedContext, targetEntity);
                    setBoundIdForBoundEntity(boundedContext, entity, configuredRepository, targetEntity);
                }
            }
        }
        return totalCount;
    }

    protected void setBoundValueByContext(BoundedContext boundedContext,
                                          Object rootEntity,
                                          ConfiguredRepository configuredRepository,
                                          Object entity) {
        EntityDefinition entityDefinition = configuredRepository.getEntityDefinition();
        for (BindingDefinition bindingDefinition : entityDefinition.getAllBindingDefinitions()) {
            if (!bindingDefinition.isBoundId()) {
                Object boundValue = getBoundValue(boundedContext, rootEntity, bindingDefinition);
                if (boundValue != null) {
                    String fieldAttribute = bindingDefinition.getFieldAttribute();
                    BeanUtil.setFieldValue(entity, fieldAttribute, boundValue);
                }
            }
        }
    }

    protected Object getBoundValue(BoundedContext boundedContext, Object rootEntity, BindingDefinition bindingDefinition) {
        if (!bindingDefinition.isFromContext()) {
            EntityPropertyChain boundEntityPropertyChain = bindingDefinition.getBoundEntityPropertyChain();
            return boundEntityPropertyChain.getValue(rootEntity);
        } else {
            String bindAttribute = bindingDefinition.getBindAttribute();
            return boundedContext.get(bindAttribute);
        }
    }

    protected void setBoundIdForBoundEntity(BoundedContext boundedContext,
                                            Object rootEntity,
                                            ConfiguredRepository configuredRepository,
                                            Object entity) {
        EntityDefinition entityDefinition = configuredRepository.getEntityDefinition();
        BindingDefinition boundIdBindingDefinition = entityDefinition.getBoundIdBindingDefinition();
        if (boundIdBindingDefinition != null) {
            EntityPropertyChain boundEntityPropertyChain = boundIdBindingDefinition.getBoundEntityPropertyChain();
            Object primaryKey = BeanUtil.getFieldValue(entity, "id");
            if (primaryKey != null) {
                boundEntityPropertyChain.setValue(rootEntity, primaryKey);
            }
        }
    }

    @Override
    public int update(BoundedContext boundedContext, E entity) {
        Assert.notNull(entity, "The entity cannot be null!");
        int totalCount = 0;
        AbstractDelegateRepository<?, ?> abstractDelegateRepository = adaptiveRepository(entity);
        for (ConfiguredRepository configuredRepository : abstractDelegateRepository.getOrderedRepositories()) {
            EntityPropertyChain entityPropertyChain = configuredRepository.getEntityPropertyChain();
            Object targetEntity = entityPropertyChain == null ? entity : entityPropertyChain.getValue(entity);
            if (targetEntity != null && isMatchScenes(boundedContext, configuredRepository)) {
                if (targetEntity instanceof Collection) {
                    for (Object eachEntity : (Collection<?>) targetEntity) {
                        totalCount += configuredRepository.update(boundedContext, eachEntity);
                    }
                } else {
                    totalCount += configuredRepository.update(boundedContext, targetEntity);
                }
            }
        }
        return totalCount;
    }

    @Override
    public int updateByExample(E entity, Object example) {
        return rootRepository.updateByExample(entity, example);
    }

    @Override
    public int delete(BoundedContext boundedContext, E entity) {
        Assert.notNull(entity, "The entity cannot be null!");
        int totalCount = 0;
        AbstractDelegateRepository<?, ?> abstractDelegateRepository = adaptiveRepository(entity);
        for (ConfiguredRepository configuredRepository : abstractDelegateRepository.getOrderedRepositories()) {
            EntityPropertyChain entityPropertyChain = configuredRepository.getEntityPropertyChain();
            Object targetEntity = entityPropertyChain == null ? entity : entityPropertyChain.getValue(entity);
            if (targetEntity != null && isMatchScenes(boundedContext, configuredRepository)) {
                if (targetEntity instanceof Collection) {
                    for (Object eachEntity : (Collection<?>) targetEntity) {
                        totalCount += configuredRepository.delete(boundedContext, eachEntity);
                    }
                } else {
                    totalCount += configuredRepository.delete(boundedContext, targetEntity);
                }
            }
        }
        return totalCount;
    }

    @Override
    public int deleteByPrimaryKey(PK primaryKey) {
        BoundedContext boundedContext = new BoundedContext();
        E entity = selectByPrimaryKey(boundedContext, primaryKey);
        return delete(boundedContext, entity);
    }

    @Override
    public int deleteByExample(Object example) {
        return rootRepository.deleteByExample(example);
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
    public int deleteList(BoundedContext boundedContext, List<E> entities) {
        return entities.stream().mapToInt(entity -> delete(boundedContext, entity)).sum();
    }

    @Override
    public int forceInsert(BoundedContext boundedContext, E entity) {
        boundedContext.put("#forceInsert", true);
        return insert(boundedContext, entity);
    }

    @Override
    public int forceInsertList(BoundedContext boundedContext, List<E> entities) {
        boundedContext.put("#forceInsert", true);
        return entities.stream().mapToInt(entity -> insert(boundedContext, entity)).sum();
    }

}
