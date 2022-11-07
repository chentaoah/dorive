package com.gitee.spring.boot.starter.domain.impl;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.gitee.spring.boot.starter.domain.entity.Metadata;
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
import com.gitee.spring.domain.core.impl.binder.PropertyBinder;
import com.gitee.spring.domain.core.impl.resolver.BinderResolver;
import com.gitee.spring.domain.core.repository.ConfiguredRepository;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

        StringBuilder sqlBuilder = new StringBuilder();
        StringBuilder sqlCriteriaBuilder = new StringBuilder();

        Map<String, String> tableAliasMap = new LinkedHashMap<>();
        String aliases = "abcdefghijklmnopqrstuvwxyz";
        int aliasIndex = 0;

        for (RepositoryWrapper repositoryWrapper : coatingWrapper.getRepositoryWrappers()) {
            RepositoryDefinition repositoryDefinition = repositoryWrapper.getRepositoryDefinition();
            String absoluteAccessPath = repositoryDefinition.getAbsoluteAccessPath();
            Example example = newExampleByCoating(repositoryWrapper, coatingObject);
            if ("/".equals(absoluteAccessPath) || example.isDirtyQuery()) {
                String tableAlias = aliases.substring(aliasIndex, aliasIndex + 1);
                aliasIndex++;
                buildSQL(sqlBuilder, tableAliasMap, tableAlias, repositoryWrapper);
                buildSQL(sqlCriteriaBuilder, tableAlias, example);
            }
        }

        if (sqlCriteriaBuilder.length() > 0) {
            sqlBuilder.append("WHERE ").append(sqlCriteriaBuilder);
        }

        return null;
    }

    private Example newExampleByCoating(RepositoryWrapper repositoryWrapper, Object coatingObject) {
        Example example = new Example();
        for (PropertyWrapper propertyWrapper : repositoryWrapper.getCollectedPropertyWrappers()) {
            Property property = propertyWrapper.getProperty();
            Field declaredField = property.getDeclaredField();
            Object fieldValue = ReflectUtil.getFieldValue(coatingObject, declaredField);
            if (fieldValue != null) {
                PropertyDefinition propertyDefinition = propertyWrapper.getPropertyDefinition();
                String alias = propertyDefinition.getAlias();
                String operator = propertyDefinition.getOperator();
                example.addCriterion(new Criterion(alias, operator, fieldValue));
            }
        }
        return example;
    }

    private void buildSQL(StringBuilder sqlBuilder, Map<String, String> tableAliasMap, String tableAlias, RepositoryWrapper repositoryWrapper) {
        RepositoryDefinition repositoryDefinition = repositoryWrapper.getRepositoryDefinition();
        String absoluteAccessPath = repositoryDefinition.getAbsoluteAccessPath();
        ConfiguredRepository definitionRepository = repositoryDefinition.getDefinitionRepository();
        ConfiguredRepository configuredRepository = repositoryDefinition.getConfiguredRepository();

        BinderResolver binderResolver = definitionRepository.getBinderResolver();
        TableInfo tableInfo = getTableInfo(configuredRepository);
        String tableName = tableInfo.getTableName();
        tableAliasMap.put(tableName, tableAlias);

        if ("/".equals(absoluteAccessPath)) {
            String sqlTemplate = "SELECT %s FROM %s %s ";
            List<String> sqlSelectList = new ArrayList<>();
            sqlSelectList.add(tableAlias + ".id");
            List<TableFieldInfo> fieldList = tableInfo.getFieldList();
            for (TableFieldInfo tableFieldInfo : fieldList) {
                sqlSelectList.add(tableAlias + "." + tableFieldInfo.getColumn());
            }
            String sqlSelect = StrUtil.join(",", sqlSelectList);
            String sqlString = String.format(sqlTemplate, sqlSelect, tableName, tableAlias);
            sqlBuilder.append(sqlString);

        } else {
            String sqlTemplate = "LEFT JOIN %s %s ON %s ";
            List<String> sqlCriteriaList = new ArrayList<>();
            for (PropertyBinder propertyBinder : binderResolver.getPropertyBinders()) {
                BindingDefinition bindingDefinition = propertyBinder.getBindingDefinition();
                String alias = StrUtil.toUnderlineCase(bindingDefinition.getAlias());
                String bindAlias = StrUtil.toUnderlineCase(bindingDefinition.getBindAlias());

                TableInfo joinTableInfo = getTableInfo(propertyBinder.getBelongRepository());
                String joinTableName = joinTableInfo.getTableName();
                String joinTableAlias = tableAliasMap.get(joinTableName);
                sqlCriteriaList.add(tableAlias + "." + alias + " = " + joinTableAlias + "." + bindAlias);
            }
            String sqlCriteria = StrUtil.join(" AND ", sqlCriteriaList);
            String sqlString = String.format(sqlTemplate, tableName, tableAlias, sqlCriteria);
            sqlBuilder.append(sqlString);
        }
    }

    private TableInfo getTableInfo(ConfiguredRepository repository) {
        Metadata metadata = (Metadata) repository.getMetadata();
        Class<?> pojoClass = metadata.getPojoClass();
        return TableInfoHelper.getTableInfo(pojoClass);
    }

    private void buildSQL(StringBuilder sqlCriteriaBuilder, String tableAlias, Example example) {
        List<String> criteria = new ArrayList<>();
        for (Criterion criterion : example.getCriteria()) {
            criteria.add(tableAlias + "." + criterion);
        }
        sqlCriteriaBuilder.append(StrUtil.join(" AND ", criteria));
    }

}
