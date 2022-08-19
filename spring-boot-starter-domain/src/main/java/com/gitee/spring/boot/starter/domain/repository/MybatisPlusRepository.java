package com.gitee.spring.boot.starter.domain.repository;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.Pair;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.gitee.spring.domain.core.entity.BoundedContext;
import com.gitee.spring.domain.core.entity.EntityDefinition;
import com.gitee.spring.domain.core.repository.AbstractRepository;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("unchecked")
public class MybatisPlusRepository extends AbstractRepository<Object, Object> {

    protected EntityDefinition entityDefinition;
    protected BaseMapper<Object> baseMapper;
    protected List<Pair<String, String>> fieldColumnPairs;

    public MybatisPlusRepository(EntityDefinition entityDefinition) {
        this.entityDefinition = entityDefinition;
        this.baseMapper = (BaseMapper<Object>) entityDefinition.getMapper();
        Class<?> pojoClass = entityDefinition.getPojoClass();
        if (pojoClass != null) {
            this.fieldColumnPairs = new ArrayList<>();
            for (Field field : ReflectUtil.getFields(pojoClass)) {
                boolean isTableId = field.isAnnotationPresent(TableId.class);
                String fieldName = field.getName();
                if (isTableId || "id".equals(fieldName)) {
                    continue;
                }
                fieldColumnPairs.add(new Pair<>(fieldName, StrUtil.toUnderlineCase(fieldName)));
            }
        }
    }

    @Override
    public Object selectByPrimaryKey(BoundedContext boundedContext, Object primaryKey) {
        return baseMapper.selectById((Serializable) primaryKey);
    }

    @Override
    public List<Object> selectByExample(BoundedContext boundedContext, Object example) {
        if (example instanceof QueryWrapper) {
            return baseMapper.selectList((QueryWrapper<Object>) example);

        } else if (example instanceof Map) {
            return baseMapper.selectByMap((Map<String, Object>) example);
        }
        return null;
    }

    @Override
    public <T> T selectPageByExample(BoundedContext boundedContext, Object example, Object page) {
        return (T) baseMapper.selectPage((IPage<Object>) page, (QueryWrapper<Object>) example);
    }

    @Override
    public int insert(BoundedContext boundedContext, Object entity) {
        return baseMapper.insert(entity);
    }

    @Override
    public int updateSelective(BoundedContext boundedContext, Object entity) {
        return baseMapper.updateById(entity);
    }

    @Override
    public int update(BoundedContext boundedContext, Object entity) {
        Object primaryKey = BeanUtil.getFieldValue(entity, "id");
        if (primaryKey != null) {
            UpdateWrapper<Object> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("id", primaryKey);
            Set<String> fieldNames = (Set<String>) boundedContext.get("#forceUpdate");
            for (Pair<String, String> fieldColumnPair : fieldColumnPairs) {
                String fieldName = fieldColumnPair.getKey();
                Object fieldValue = BeanUtil.getFieldValue(entity, fieldName);
                if (fieldValue != null || (fieldNames != null && fieldNames.contains(fieldName))) {
                    updateWrapper.set(true, fieldColumnPair.getValue(), fieldValue);
                }
            }
            return baseMapper.update(null, updateWrapper);
        }
        return 0;
    }

    @Override
    public int updateByExample(BoundedContext boundedContext, Object entity, Object example) {
        return baseMapper.update(entity, (Wrapper<Object>) example);
    }

    @Override
    public int insertOrUpdate(BoundedContext boundedContext, Object entity) {
        throw new RuntimeException("This method is not supported!");
    }

    @Override
    public int delete(BoundedContext boundedContext, Object entity) {
        Object primaryKey = BeanUtil.getFieldValue(entity, "id");
        return deleteByPrimaryKey(boundedContext, primaryKey);
    }

    @Override
    public int deleteByPrimaryKey(BoundedContext boundedContext, Object primaryKey) {
        return baseMapper.deleteById((Serializable) primaryKey);
    }

    @Override
    public int deleteByExample(BoundedContext boundedContext, Object example) {
        return baseMapper.delete((QueryWrapper<Object>) example);
    }

}
