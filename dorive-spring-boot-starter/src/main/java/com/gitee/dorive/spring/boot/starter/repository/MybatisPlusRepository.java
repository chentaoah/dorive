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
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.gitee.dorive.api.entity.def.EntityDef;
import com.gitee.dorive.api.entity.element.EntityEle;
import com.gitee.dorive.core.api.common.EntityFactory;
import com.gitee.dorive.core.api.executor.Executor;
import com.gitee.dorive.core.impl.factory.DefaultEntityFactory;
import com.gitee.dorive.ref.repository.AbstractRefRepository;
import com.gitee.dorive.spring.boot.starter.api.Keys;
import com.gitee.dorive.spring.boot.starter.impl.CountQuerier;
import com.gitee.dorive.spring.boot.starter.impl.SQLExampleBuilder;
import com.gitee.dorive.spring.boot.starter.impl.executor.AliasExecutor;
import com.gitee.dorive.spring.boot.starter.impl.executor.FactoryExecutor;
import com.gitee.dorive.spring.boot.starter.impl.executor.MybatisPlusExecutor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = false)
public class MybatisPlusRepository<E, PK> extends AbstractRefRepository<E, PK> {

    private CountQuerier countQuerier;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        if ("SQL".equals(getQuerier())) {
            setExampleBuilder(new SQLExampleBuilder(this));
        }
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
        assert tableInfo != null;
        attachments.put(Keys.TABLE_INFO, tableInfo);

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

            Map<String, String> aliasFieldMapping = entityEle.newAliasFieldMapping();
            defaultEntityFactory.setAliasFieldMapping(aliasFieldMapping);

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
            defaultEntityFactory.setFieldPropMapping(fieldPropMapping);
        }

        Executor executor = new MybatisPlusExecutor(entityDef, entityEle, (BaseMapper<Object>) mapper, (Class<Object>) pojoClass);
        executor = new FactoryExecutor(entityEle, entityFactory, executor);
        executor = new AliasExecutor(entityEle, executor);
        attachments.put(Keys.ALIAS_EXECUTOR, executor);
        return executor;
    }

}
