package com.gitee.spring.boot.starter.domain.impl;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.toolkit.SqlRunner;
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
import java.util.Collections;
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

        Page<Object> pageInfo = coatingWrapper.getPageInfo(coatingObject);

        char letter = 'a';

        Map<String, SqlSegment> sqlSegmentMap = new LinkedHashMap<>();
        SqlSegment rootSqlSegment = null;
        List<SqlSegment> sqlCriteria = new ArrayList<>();

        for (RepositoryWrapper repositoryWrapper : coatingWrapper.getRepositoryWrappers()) {
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

            boolean isToHandle = example.isDirtyQuery();
            if (isToHandle) {
                for (Criterion criterion : example.getCriteria()) {
                    sqlCriteria.add(new SqlSegment(tableAlias + "." + criterion, tableName, tableAlias, true, Collections.emptySet()));
                }
            }

            if ("/".equals(absoluteAccessPath)) {
                String sql = String.format("SELECT %s.id FROM %s %s ", tableAlias, tableName, tableAlias);
                rootSqlSegment = new SqlSegment(sql, tableName, tableAlias, isToHandle, new HashSet<>(8));
                sqlSegmentMap.put(tableName, rootSqlSegment);

            } else {
                String sql = buildSQL(sqlSegmentMap, tableName, tableAlias, binderResolver);
                sqlSegmentMap.put(tableName, new SqlSegment(sql, tableName, tableAlias, isToHandle, new HashSet<>(8)));
            }
        }

        assert rootSqlSegment != null;
        findAllSqlToHandle(sqlSegmentMap, rootSqlSegment);

        StringBuilder sqlBuilder = new StringBuilder();
        if (!sqlSegmentMap.isEmpty()) {
            for (SqlSegment sqlSegment : sqlSegmentMap.values()) {
                if (sqlSegment.isToHandle()) {
                    sqlBuilder.append(sqlSegment.getSql());
                }
            }
            if (!sqlCriteria.isEmpty()) {
                sqlBuilder.append("WHERE ").append(StrUtil.join(" AND ", sqlCriteria));
            }
            if (pageInfo != null) {
                sqlBuilder.append(" ").append(pageInfo);
            }
        }

        Example example = new Example();

        if (sqlBuilder.length() == 0) {
            example.setEmptyQuery(true);
            return example;
        }

        List<Map<String, Object>> resultMaps = SqlRunner.db().selectList(sqlBuilder.toString());
        List<Object> primaryKeys = new ArrayList<>(resultMaps.size());
        for (Map<String, Object> resultMap : resultMaps) {
            Object primaryKey = resultMap.get("id");
            primaryKeys.add(primaryKey);
        }

        if (primaryKeys.isEmpty()) {
            example.setEmptyQuery(true);
            return example;
        }

        return example.eq("id", primaryKeys);
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

    private String buildSQL(Map<String, SqlSegment> sqlSegmentMap, String tableName, String tableAlias, BinderResolver binderResolver) {
        List<String> sqlCriteriaList = new ArrayList<>();
        for (PropertyBinder propertyBinder : binderResolver.getPropertyBinders()) {
            BindingDefinition bindingDefinition = propertyBinder.getBindingDefinition();
            String alias = StrUtil.toUnderlineCase(bindingDefinition.getAlias());

            TableInfo joinTableInfo = getTableInfo(propertyBinder.getBelongRepository());
            String joinTableName = joinTableInfo.getTableName();

            SqlSegment sqlSegment = sqlSegmentMap.get(joinTableName);
            if (sqlSegment != null) {
                Set<String> dependentTables = sqlSegment.getDependentTables();
                dependentTables.add(tableName);

                String joinTableAlias = sqlSegment.getTableAlias();
                String bindAlias = StrUtil.toUnderlineCase(bindingDefinition.getBindAlias());
                sqlCriteriaList.add(tableAlias + "." + alias + " = " + joinTableAlias + "." + bindAlias);
            }
        }
        String sqlCriteria = StrUtil.join(" AND ", sqlCriteriaList);
        return String.format("LEFT JOIN %s %s ON %s ", tableName, tableAlias, sqlCriteria);
    }

    private TableInfo getTableInfo(ConfiguredRepository repository) {
        Metadata metadata = (Metadata) repository.getMetadata();
        Class<?> pojoClass = metadata.getPojoClass();
        return TableInfoHelper.getTableInfo(pojoClass);
    }

    private void findAllSqlToHandle(Map<String, SqlSegment> sqlSegmentMap, SqlSegment lastSqlSegment) {
        Set<String> dependentTables = lastSqlSegment.getDependentTables();
        for (String dependentTable : dependentTables) {
            SqlSegment sqlSegment = sqlSegmentMap.get(dependentTable);
            if (sqlSegment != null) {
                findAllSqlToHandle(sqlSegmentMap, sqlSegment);
                if (sqlSegment.isToHandle()) {
                    lastSqlSegment.setToHandle(true);
                }
            }
        }
    }

}
