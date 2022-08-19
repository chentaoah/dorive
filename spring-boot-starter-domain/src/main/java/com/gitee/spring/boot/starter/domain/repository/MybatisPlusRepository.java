package com.gitee.spring.boot.starter.domain.repository;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.gitee.spring.domain.core.entity.BoundedContext;
import com.gitee.spring.domain.core.entity.EntityDefinition;
import com.gitee.spring.domain.core.repository.AbstractRepository;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unchecked")
public class MybatisPlusRepository extends AbstractRepository<Object, Object> {

    protected EntityDefinition entityDefinition;
    protected BaseMapper<Object> baseMapper;

    public MybatisPlusRepository(EntityDefinition entityDefinition, BaseMapper<Object> baseMapper) {
        this.entityDefinition = entityDefinition;
        this.baseMapper = baseMapper;
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
        Object primaryKey = BeanUtil.getFieldValue(entity, "id");
        QueryWrapper<Object> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", primaryKey);
        return baseMapper.update(entity, queryWrapper);
    }
    
    @Override
    public int update(BoundedContext boundedContext, Object entity) {
        return baseMapper.updateById(entity);
    }

    @Override
    public int updateByExample(BoundedContext boundedContext, Object entity, Object example) {
        return baseMapper.update(entity, (QueryWrapper<Object>) example);
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
