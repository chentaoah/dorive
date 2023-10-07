/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gitee.dorive.spring.boot.starter.repository;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ReflectUtil;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.gitee.dorive.api.entity.def.EntityDef;
import com.gitee.dorive.api.entity.def.FieldDef;
import com.gitee.dorive.api.entity.element.EntityEle;
import com.gitee.dorive.api.entity.element.EntityField;
import com.gitee.dorive.coating.api.ExampleBuilder;
import com.gitee.dorive.coating.entity.BuildExample;
import com.gitee.dorive.core.api.context.Context;
import com.gitee.dorive.core.api.executor.FieldConverter;
import com.gitee.dorive.core.api.executor.EntityFactory;
import com.gitee.dorive.core.api.executor.Executor;
import com.gitee.dorive.core.impl.converter.DefaultFieldConverter;
import com.gitee.dorive.core.impl.factory.DefaultEntityFactory;
import com.gitee.dorive.ref.repository.AbstractRefRepository;
import com.gitee.dorive.spring.boot.starter.api.Keys;
import com.gitee.dorive.spring.boot.starter.impl.CountQuerier;
import com.gitee.dorive.spring.boot.starter.impl.SQLExampleBuilder;
import com.gitee.dorive.core.impl.executor.FactoryExecutor;
import com.gitee.dorive.core.impl.executor.FieldExecutor;
import com.gitee.dorive.spring.boot.starter.impl.executor.MybatisPlusExecutor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@EqualsAndHashCode(callSuper = false)
public class MybatisPlusRepository<E, PK> extends AbstractRefRepository<E, PK> {

    private ExampleBuilder sqlExampleBuilder;
    private CountQuerier countQuerier;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        this.sqlExampleBuilder = new SQLExampleBuilder(this);
        this.countQuerier = new CountQuerier(this);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Executor newExecutor(EntityDef entityDef, EntityEle entityEle, Map<String, Object> attachments) {
        Class<?> mapperClass = entityDef.getSource();
        Object mapper = null;
        Class<?> pojoClass = null;
        if (mapperClass != Object.class) {
            mapper = getApplicationContext().getBean(mapperClass);
            Type[] genericInterfaces = mapperClass.getGenericInterfaces();
            if (genericInterfaces.length > 0) {
                Type genericInterface = mapperClass.getGenericInterfaces()[0];
                if (genericInterface instanceof ParameterizedType) {
                    ParameterizedType parameterizedType = (ParameterizedType) genericInterface;
                    Type actualTypeArgument = parameterizedType.getActualTypeArguments()[0];
                    pojoClass = (Class<?>) actualTypeArgument;
                }
            }
        }
        Assert.notNull(pojoClass, "The class of pojo cannot be null! source: {}", mapperClass);

        TableInfo tableInfo = TableInfoHelper.getTableInfo(pojoClass);
        attachments.put(Keys.TABLE_INFO, tableInfo);
        Map<String, FieldConverter> converterMap = newConverterMap(entityEle);
        EntityFactory entityFactory = newEntityFactory(entityDef, entityEle, pojoClass, tableInfo, converterMap);

        Executor executor = new MybatisPlusExecutor(entityDef, entityEle, (BaseMapper<Object>) mapper, (Class<Object>) pojoClass);
        executor = new FactoryExecutor(executor, entityEle, entityFactory);
        executor = new FieldExecutor(executor, entityEle, converterMap);
        attachments.put(Keys.FIELD_EXECUTOR, executor);
        executor = processExecutor(entityDef, entityEle, executor);
        return executor;
    }

    private Map<String, FieldConverter> newConverterMap(EntityEle entityEle) {
        Map<String, FieldConverter> converterMap = new LinkedHashMap<>(8);
        Map<String, EntityField> entityFieldMap = entityEle.getEntityFieldMap();
        if (entityFieldMap != null) {
            entityFieldMap.forEach((name, entityField) -> {
                FieldDef fieldDef = entityField.getFieldDef();
                if (fieldDef != null) {
                    Class<?> converterClass = fieldDef.getConverter();
                    String mapExp = fieldDef.getMapExp();
                    FieldConverter fieldConverter = null;
                    if (converterClass != Object.class) {
                        fieldConverter = (FieldConverter) ReflectUtil.newInstance(converterClass);

                    } else if (StringUtils.isNotBlank(mapExp)) {
                        fieldConverter = new DefaultFieldConverter(entityField);
                    }
                    if (fieldConverter != null) {
                        converterMap.put(name, fieldConverter);
                    }
                }
            });
        }
        return converterMap;
    }

    private EntityFactory newEntityFactory(EntityDef entityDef, EntityEle entityEle, Class<?> pojoClass, TableInfo tableInfo,
                                           Map<String, FieldConverter> converterMap) {
        Class<?> factoryClass = entityDef.getFactory();
        EntityFactory entityFactory;
        if (factoryClass == Object.class) {
            entityFactory = new DefaultEntityFactory();
        } else {
            entityFactory = (EntityFactory) getApplicationContext().getBean(factoryClass);
        }
        if (entityFactory instanceof DefaultEntityFactory) {
            DefaultEntityFactory defaultEntityFactory = (DefaultEntityFactory) entityFactory;
            defaultEntityFactory.setEntityEle(entityEle);
            defaultEntityFactory.setPojoClass(pojoClass);

            Map<String, String> aliasFieldMapping = newAliasFieldMapping(entityEle);
            defaultEntityFactory.setReCopyOptions(aliasFieldMapping, converterMap);

            Map<String, String> fieldPropMapping = newFieldPropMapping(tableInfo, aliasFieldMapping);
            defaultEntityFactory.setDeCopyOptions(fieldPropMapping, newPropConverterMap(fieldPropMapping, converterMap));
        }
        return entityFactory;
    }

    private Map<String, String> newAliasFieldMapping(EntityEle entityEle) {
        Map<String, String> fieldAliasMap = entityEle.getFieldAliasMap();
        return fieldAliasMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
    }

    private Map<String, String> newFieldPropMapping(TableInfo tableInfo, Map<String, String> aliasFieldMapping) {
        String keyColumn = tableInfo.getKeyColumn();
        String keyProperty = tableInfo.getKeyProperty();
        List<TableFieldInfo> tableFieldInfos = tableInfo.getFieldList();
        Map<String, String> fieldPropMapping = new LinkedHashMap<>();

        if (StringUtils.isNotBlank(keyColumn) && StringUtils.isNotBlank(keyProperty)) {
            String field = aliasFieldMapping.get(keyColumn);
            if (field != null) {
                fieldPropMapping.put(field, keyProperty);
            }
        }
        for (TableFieldInfo tableFieldInfo : tableFieldInfos) {
            String field = aliasFieldMapping.get(tableFieldInfo.getColumn());
            if (field != null) {
                fieldPropMapping.put(field, tableFieldInfo.getProperty());
            }
        }
        return fieldPropMapping;
    }

    private Map<String, FieldConverter> newPropConverterMap(Map<String, String> fieldPropMapping, Map<String, FieldConverter> converterMap) {
        Map<String, FieldConverter> propConverterMap = new LinkedHashMap<>(converterMap.size());
        converterMap.forEach((field, fieldConverter) -> {
            String prop = fieldPropMapping.get(field);
            propConverterMap.put(prop, fieldConverter);
        });
        return propConverterMap;
    }

    protected Executor processExecutor(EntityDef entityDef, EntityEle entityEle, Executor executor) {
        return executor;
    }

    @Override
    public BuildExample buildExample(Context context, Object coating) {
        Map<String, Object> attachments = context.getAttachments();
        String querier = (String) attachments.get(Keys.QUERIER);
        if (querier == null || "SQL".equals(querier)) {
            return sqlExampleBuilder.buildExample(context, coating);
        } else {
            return getExampleBuilder().buildExample(context, coating);
        }
    }

}
