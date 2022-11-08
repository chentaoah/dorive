package com.gitee.spring.boot.starter.domain.impl;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.toolkit.SqlRunner;
import com.gitee.spring.boot.starter.domain.entity.JoinSegment;
import com.gitee.spring.boot.starter.domain.entity.Metadata;
import com.gitee.spring.boot.starter.domain.entity.SqlSegment;
import com.gitee.spring.domain.coating.api.ExampleBuilder;
import com.gitee.spring.domain.coating.entity.CoatingWrapper;
import com.gitee.spring.domain.coating.entity.PropertyWrapper;
import com.gitee.spring.domain.coating.entity.RepositoryWrapper;
import com.gitee.spring.domain.coating.entity.definition.PropertyDefinition;
import com.gitee.spring.domain.coating.entity.definition.RepositoryDefinition;
import com.gitee.spring.domain.coating.impl.resolver.CoatingWrapperResolver;
import com.gitee.spring.domain.coating.repository.AbstractCoatingRepository;
import com.gitee.spring.domain.core.entity.BoundedContext;
import com.gitee.spring.domain.core.entity.Property;
import com.gitee.spring.domain.core.entity.definition.BindingDefinition;
import com.gitee.spring.domain.core.entity.executor.Criterion;
import com.gitee.spring.domain.core.entity.executor.Example;
import com.gitee.spring.domain.core.entity.executor.Page;
import com.gitee.spring.domain.core.impl.binder.ContextBinder;
import com.gitee.spring.domain.core.impl.binder.PropertyBinder;
import com.gitee.spring.domain.core.impl.resolver.BinderResolver;
import com.gitee.spring.domain.core.repository.ConfiguredRepository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SQLExampleBuilder implements ExampleBuilder {

    private final AbstractCoatingRepository<?, ?> repository;

    public SQLExampleBuilder(AbstractCoatingRepository<?, ?> repository) {
        this.repository = repository;
    }

    @Override
    public Example buildExample(BoundedContext boundedContext, Object coatingObject) {
        CoatingWrapperResolver coatingWrapperResolver = repository.getCoatingWrapperResolver();
        Map<Class<?>, CoatingWrapper> coatingWrapperMap = coatingWrapperResolver.getCoatingWrapperMap();

        CoatingWrapper coatingWrapper = coatingWrapperMap.get(coatingObject.getClass());
        Assert.notNull(coatingWrapper, "No coating wrapper exists!");
        List<RepositoryWrapper> repositoryWrappers = coatingWrapper.getRepositoryWrappers();

        Map<String, SqlSegment> sqlSegmentMap = new LinkedHashMap<>(repositoryWrappers.size() * 4 / 3 + 1);
        SqlSegment rootSqlSegment = null;
        Page<Object> pageInfo = coatingWrapper.getPageInfo(coatingObject);
        char letter = 'a';

        for (RepositoryWrapper repositoryWrapper : repositoryWrappers) {
            RepositoryDefinition repositoryDefinition = repositoryWrapper.getRepositoryDefinition();
            String absoluteAccessPath = repositoryDefinition.getAbsoluteAccessPath();
            ConfiguredRepository definitionRepository = repositoryDefinition.getDefinitionRepository();
            ConfiguredRepository configuredRepository = repositoryDefinition.getConfiguredRepository();

            BinderResolver binderResolver = definitionRepository.getBinderResolver();

            Example example = newExampleByCoating(repositoryWrapper, coatingObject);
            if (example.isDirtyQuery()) {
                appendCriteriaByContext(boundedContext, repositoryWrapper, example);
            }

            TableInfo tableInfo = getTableInfo(configuredRepository);
            String tableName = tableInfo.getTableName();

            String tableAlias = String.valueOf(letter);
            letter = (char) (letter + 1);

            List<JoinSegment> joinSegments = getJoinSegments(sqlSegmentMap, binderResolver, tableName, tableAlias);
            String sqlCriteria = example.isDirtyQuery() ? buildSqlCriteria(tableAlias, example) : null;

            if ("/".equals(absoluteAccessPath)) {
                String sql = String.format("SELECT %s.id FROM %s %s ", tableAlias, tableName, tableAlias);
                rootSqlSegment = new SqlSegment(tableName, tableAlias, sql, joinSegments, sqlCriteria, true, example.isDirtyQuery(), new HashSet<>(4));
                sqlSegmentMap.put(tableName, rootSqlSegment);

            } else {
                String sql = String.format("LEFT JOIN %s %s ON ", tableName, tableAlias);
                SqlSegment sqlSegment = new SqlSegment(tableName, tableAlias, sql, joinSegments, sqlCriteria, false, example.isDirtyQuery(), new HashSet<>(4));
                sqlSegmentMap.put(tableName, sqlSegment);
            }
        }

        assert rootSqlSegment != null;
        markReachableAndDirty(sqlSegmentMap, rootSqlSegment);

        if (!rootSqlSegment.isDirtyQuery()) {
            return new Example();
        }

        StringBuilder sqlBuilder = new StringBuilder();
        List<String> sqlCriteria = new ArrayList<>(sqlSegmentMap.size());
        for (SqlSegment sqlSegment : sqlSegmentMap.values()) {
            if (sqlSegment.isRootReachable() && sqlSegment.isDirtyQuery()) {
                sqlBuilder.append(sqlSegment);

                List<JoinSegment> joinSegments = getAvailableJoinSegments(sqlSegmentMap, sqlSegment);
                if (!joinSegments.isEmpty()) {
                    sqlBuilder.append(StrUtil.join(" AND ", joinSegments)).append(" ");
                }

                if (sqlSegment.getSqlCriteria() != null) {
                    sqlCriteria.add(sqlSegment.getSqlCriteria());
                }
            }
        }
        sqlBuilder.append("WHERE ").append(StrUtil.join(" AND ", sqlCriteria));
        if (pageInfo != null) {
            sqlBuilder.append(" ").append(pageInfo);
        }

        Example example = new Example();
        example.setPage(pageInfo);

        List<Map<String, Object>> resultMaps = SqlRunner.db().selectList(sqlBuilder.toString());
        if (!resultMaps.isEmpty()) {
            List<Object> primaryKeys = new ArrayList<>(resultMaps.size());
            for (Map<String, Object> resultMap : resultMaps) {
                Object primaryKey = resultMap.get("id");
                primaryKeys.add(primaryKey);
            }
            return example.eq("id", primaryKeys);

        } else {
            example.setEmptyQuery(true);
            return example;
        }
    }

    private Example newExampleByCoating(RepositoryWrapper repositoryWrapper, Object coatingObject) {
        Example example = new Example();
        for (PropertyWrapper propertyWrapper : repositoryWrapper.getCollectedPropertyWrappers()) {
            Property property = propertyWrapper.getProperty();
            Object fieldValue = property.getFieldValue(coatingObject);
            if (fieldValue != null) {
                PropertyDefinition propertyDefinition = propertyWrapper.getPropertyDefinition();
                String alias = propertyDefinition.getAlias();
                String operator = propertyDefinition.getOperator();
                example.addCriterion(new Criterion(alias, operator, fieldValue));
            }
        }
        return example;
    }

    private void appendCriteriaByContext(BoundedContext boundedContext, RepositoryWrapper repositoryWrapper, Example example) {
        RepositoryDefinition repositoryDefinition = repositoryWrapper.getRepositoryDefinition();
        ConfiguredRepository definitionRepository = repositoryDefinition.getDefinitionRepository();
        BinderResolver binderResolver = definitionRepository.getBinderResolver();
        for (ContextBinder contextBinder : binderResolver.getContextBinders()) {
            Object boundValue = contextBinder.getBoundValue(boundedContext, null);
            if (boundValue != null) {
                BindingDefinition bindingDefinition = contextBinder.getBindingDefinition();
                String alias = bindingDefinition.getAlias();
                example.eq(alias, boundValue);
            }
        }
    }

    private TableInfo getTableInfo(ConfiguredRepository repository) {
        Metadata metadata = (Metadata) repository.getMetadata();
        Class<?> pojoClass = metadata.getPojoClass();
        return TableInfoHelper.getTableInfo(pojoClass);
    }

    private List<JoinSegment> getJoinSegments(Map<String, SqlSegment> sqlSegmentMap, BinderResolver binderResolver, String tableName, String tableAlias) {
        List<PropertyBinder> propertyBinders = binderResolver.getPropertyBinders();
        List<JoinSegment> joinSegments = new ArrayList<>(propertyBinders.size());
        for (PropertyBinder propertyBinder : propertyBinders) {
            TableInfo joinTableInfo = getTableInfo(propertyBinder.getBelongRepository());
            String joinTableName = joinTableInfo.getTableName();

            SqlSegment sqlSegment = sqlSegmentMap.get(joinTableName);
            if (sqlSegment != null) {
                String joinTableAlias = sqlSegment.getTableAlias();
                Set<String> joinTableNames = sqlSegment.getJoinTableNames();
                joinTableNames.add(tableName);

                BindingDefinition bindingDefinition = propertyBinder.getBindingDefinition();
                String alias = StrUtil.toUnderlineCase(bindingDefinition.getAlias());
                String bindAlias = StrUtil.toUnderlineCase(bindingDefinition.getBindAlias());

                String sqlCriteria = tableAlias + "." + alias + " = " + joinTableAlias + "." + bindAlias;
                JoinSegment joinSegment = new JoinSegment(joinTableName, joinTableAlias, sqlCriteria);
                joinSegments.add(joinSegment);
            }
        }
        return joinSegments;
    }

    private String buildSqlCriteria(String tableAlias, Example example) {
        List<Criterion> criteria = example.getCriteria();
        List<String> sqlCriteria = new ArrayList<>(criteria.size());
        for (Criterion criterion : criteria) {
            sqlCriteria.add(tableAlias + "." + criterion);
        }
        return StrUtil.join(" AND ", sqlCriteria);
    }

    private void markReachableAndDirty(Map<String, SqlSegment> sqlSegmentMap, SqlSegment lastSqlSegment) {
        Set<String> joinTableNames = lastSqlSegment.getJoinTableNames();
        for (String joinTableName : joinTableNames) {
            SqlSegment joinSqlSegment = sqlSegmentMap.get(joinTableName);
            if (joinSqlSegment != null) {
                joinSqlSegment.setRootReachable(true);
                markReachableAndDirty(sqlSegmentMap, joinSqlSegment);
                if (joinSqlSegment.isDirtyQuery()) {
                    lastSqlSegment.setDirtyQuery(true);
                }
            }
        }
    }

    private List<JoinSegment> getAvailableJoinSegments(Map<String, SqlSegment> sqlSegmentMap, SqlSegment sqlSegment) {
        List<JoinSegment> joinSegments = sqlSegment.getJoinSegments();
        List<JoinSegment> availableJoinSegments = new ArrayList<>(joinSegments.size());
        for (JoinSegment joinSegment : joinSegments) {
            String joinTableName = joinSegment.getJoinTableName();
            SqlSegment joinSqlSegment = sqlSegmentMap.get(joinTableName);
            if (joinSqlSegment.isRootReachable() && joinSqlSegment.isDirtyQuery()) {
                availableJoinSegments.add(joinSegment);
            }
        }
        return availableJoinSegments;
    }

}
