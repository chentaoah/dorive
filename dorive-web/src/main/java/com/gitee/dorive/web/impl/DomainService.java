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

package com.gitee.dorive.web.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.ser.BeanSerializerFactory;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.gitee.dorive.api.annotation.core.Entity;
import com.gitee.dorive.core.api.context.Selector;
import com.gitee.dorive.core.entity.executor.Page;
import com.gitee.dorive.query.repository.AbstractQueryRepository;
import com.gitee.dorive.web.entity.QueryConfig;
import com.gitee.dorive.web.entity.QueryContext;
import com.gitee.dorive.web.entity.ResObject;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DomainService {

    private final Map<String, QueryConfig> idQueryConfigMap = new ConcurrentHashMap<>();

    public void executeQuery(QueryContext queryContext) throws IOException {
        HttpServletResponse response = queryContext.getResponse();
        String methodName = queryContext.getMethodName();
        String entityName = queryContext.getEntityName();
        String configId = queryContext.getConfigId();
        Map<String, Object> params = queryContext.getParams();

        QueryConfig queryConfig = idQueryConfigMap.get(configId);
        if (queryConfig == null) {
            returnMessage(response, "没有找到配置信息！");
            return;
        }
        Class<?> entityClass = queryConfig.getEntityClass();
        if (!entityName.equals(entityClass.getSimpleName())) {
            returnMessage(response, "实体配置不匹配！");
            return;
        }

        AbstractQueryRepository<?, ?> repository = queryConfig.getRepository();
        Selector selector = queryConfig.getSelector();
        Object query = BeanUtil.toBean(params, queryConfig.getQueryClass());
        Object data = null;
        if ("list".equals(methodName)) {
            List<?> entities = repository.selectByQuery(selector, query);
            data = ResObject.successData(entities);

        } else if ("query".equals(methodName)) {
            Page<?> page = repository.selectPageByQuery(selector, query);
            data = ResObject.successData(page);
        }

        ObjectMapper objectMapper = new ObjectMapper();
        addFilters(objectMapper, queryConfig.getFilterIdPropertiesMap());
        objectMapper.writeValue(response.getOutputStream(), data);
    }

    private void returnMessage(HttpServletResponse response, String message) throws IOException {
        ResObject<?> resObject = ResObject.failMsg(message);
        response.getWriter().write(JSONUtil.toJsonStr(resObject));
    }

    private void addFilters(ObjectMapper objectMapper, Map<String, List<String>> filterIdPropertiesMap) {
        objectMapper.setSerializerFactory(FilterBeanSerializerFactory.instance);
        SimpleFilterProvider simpleFilterProvider = new SimpleFilterProvider();
        filterIdPropertiesMap.forEach((filterId, properties) ->
                simpleFilterProvider.addFilter(filterId, SimpleBeanPropertyFilter.filterOutAllExcept(properties.toArray(new String[0]))));
        objectMapper.setFilterProvider(simpleFilterProvider);
    }

    public static class FilterBeanSerializerFactory extends BeanSerializerFactory {
        public static FilterBeanSerializerFactory instance = new FilterBeanSerializerFactory();

        protected FilterBeanSerializerFactory() {
            super(null);
        }

        @Override
        protected Object findFilterId(SerializationConfig config, BeanDescription beanDesc) {
            Class<?> beanClass = beanDesc.getBeanClass();
            if (beanClass.isAnnotationPresent(Entity.class)) {
                return beanClass.getName() + ".Filter";
            }
            return super.findFilterId(config, beanDesc);
        }
    }

}
