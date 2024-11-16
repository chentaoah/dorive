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
import cn.hutool.core.util.ClassLoaderUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.http.ContentType;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.ser.BeanSerializerFactory;
import com.fasterxml.jackson.databind.ser.PropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.gitee.dorive.api.annotation.core.Entity;
import com.gitee.dorive.api.entity.core.EntityElement;
import com.gitee.dorive.core.api.context.Selector;
import com.gitee.dorive.core.entity.executor.Page;
import com.gitee.dorive.core.repository.CommonRepository;
import com.gitee.dorive.query.entity.MergedRepository;
import com.gitee.dorive.query.impl.resolver.MergedRepositoryResolver;
import com.gitee.dorive.query.repository.AbstractQueryRepository;
import com.gitee.dorive.web.entity.ExporterConfig;
import com.gitee.dorive.web.entity.ResObject;
import com.gitee.dorive.web.entity.req.ListOrPageReq;
import com.gitee.dorive.web.entity.req.LoadConfigReq;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DomainService {

    private final ApplicationContext applicationContext;
    private final Map<String, ExporterConfig> urlExporterConfigMap;

    public DomainService(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        this.urlExporterConfigMap = new ConcurrentHashMap<>();
    }

    public ResObject<Object> loadConfigs(List<LoadConfigReq> loadConfigReqs) {
        for (LoadConfigReq loadConfigReq : loadConfigReqs) {
            ResObject<Object> resObject = loadConfig(loadConfigReq);
            if (resObject.isFail()) {
                return resObject;
            }
        }
        return ResObject.successMsg("加载成功！");
    }

    private ResObject<Object> loadConfig(LoadConfigReq loadConfigReq) {
        String url = loadConfigReq.getUrl();
        String entityType = loadConfigReq.getEntityType();
        String selectorName = loadConfigReq.getSelectorName();
        String repositoryType = loadConfigReq.getRepositoryType();
        String queryType = loadConfigReq.getQueryType();

        Class<?> entityClass;
        Class<?> repositoryClass;
        Class<?> queryClass;
        try {
            entityClass = ClassLoaderUtil.loadClass(entityType);
            repositoryClass = ClassLoaderUtil.loadClass(repositoryType);
            queryClass = ClassLoaderUtil.loadClass(queryType);

        } catch (Exception e) {
            return ResObject.failMsg("类型加载失败！");
        }

        AbstractQueryRepository<?, ?> repository = (AbstractQueryRepository<?, ?>) applicationContext.getBean(repositoryClass);
        MergedRepositoryResolver mergedRepositoryResolver = repository.getMergedRepositoryResolver();
        Map<String, MergedRepository> nameMergedRepositoryMap = mergedRepositoryResolver.getNameMergedRepositoryMap();

        Field staticField = ReflectUtil.getField(entityClass, selectorName);
        if (staticField == null) {
            return ResObject.failMsg("选取器不存在！selectorName: " + selectorName);
        }
        Object value = ReflectUtil.getStaticFieldValue(staticField);
        Selector selector = (Selector) value;

        Map<String, List<String>> filterIdPropertiesMap = new LinkedHashMap<>(8);
        Set<String> names = selector.getNames();
        for (String entityName : names) {
            MergedRepository mergedRepository = nameMergedRepositoryMap.get(entityName);
            CommonRepository definedRepository = mergedRepository.getDefinedRepository();
            EntityElement entityElement = definedRepository.getEntityElement();
            Class<?> genericType = entityElement.getGenericType();
            String filterId = genericType.getName();
            List<String> properties = filterIdPropertiesMap.computeIfAbsent(filterId, key -> new ArrayList<>(4));
            List<String> select = selector.select(entityName);
            if (select == null || select.isEmpty()) {
                Field[] fields = ReflectUtil.getFieldsDirectly(genericType, true);
                for (Field field : fields) {
                    if (!Modifier.isStatic(field.getModifiers())) {
                        properties.add(field.getName());
                    }
                }
            } else {
                properties.addAll(select);
            }

            // 补充内部实体的字段
            Field field = entityElement.getJavaField();
            if (field != null) {
                Class<?> declaringClass = field.getDeclaringClass();
                String declaringFilterId = declaringClass.getName();
                List<String> declaringProperties = filterIdPropertiesMap.computeIfAbsent(declaringFilterId, key -> new ArrayList<>(4));
                declaringProperties.add(field.getName());
            }
        }

        ExporterConfig exporterConfig = new ExporterConfig();
        exporterConfig.setUrl(url);
        exporterConfig.setEntityClass(entityClass);
        exporterConfig.setSelector(selector);
        exporterConfig.setRepository(repository);
        exporterConfig.setQueryClass(queryClass);
        exporterConfig.setFilterIdPropertiesMap(filterIdPropertiesMap);
        urlExporterConfigMap.put(url, exporterConfig);

        return ResObject.successMsg("加载成功！");
    }

    public void executeQuery(ListOrPageReq listOrPageReq) throws IOException {
        HttpServletResponse response = listOrPageReq.getResponse();
        String methodName = listOrPageReq.getMethodName();
        String entity = listOrPageReq.getEntity();
        String config = listOrPageReq.getConfig();
        Map<String, Object> params = listOrPageReq.getParams();

        response.setHeader(HttpHeaders.CONTENT_TYPE, ContentType.JSON.getValue());

        ExporterConfig exporterConfig = urlExporterConfigMap.get(entity + "/" + config);
        if (exporterConfig == null) {
            failMsg(response, "没有找到配置信息！");
            return;
        }

        AbstractQueryRepository<?, ?> repository = exporterConfig.getRepository();
        Selector selector = exporterConfig.getSelector();
        Object query = BeanUtil.toBean(params, exporterConfig.getQueryClass());
        Object data = null;
        try {
            if ("list".equals(methodName)) {
                List<?> entities = repository.selectByQuery(selector, query);
                data = ResObject.successData(entities);

            } else if ("page".equals(methodName)) {
                Page<?> page = repository.selectPageByQuery(selector, query);
                data = ResObject.successData(page);
            }

        } catch (Exception e) {
            failMsg(response, e.getMessage());
            return;
        }

        ObjectMapper objectMapper = new ObjectMapper();
        addFilters(objectMapper, exporterConfig.getFilterIdPropertiesMap());
        objectMapper.writeValue(response.getOutputStream(), data);
    }

    private void failMsg(HttpServletResponse response, String message) throws IOException {
        ResObject<?> resObject = ResObject.failMsg(message);
        response.getWriter().write(JSONUtil.toJsonStr(resObject));
    }

    private void addFilters(ObjectMapper objectMapper, Map<String, List<String>> filterIdPropertiesMap) {
        objectMapper.setSerializerFactory(FilterBeanSerializerFactory.instance);
        SimpleFilterProvider simpleFilterProvider = new SuperclassFilterProvider();
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
                return beanClass.getName();
            }
            return super.findFilterId(config, beanDesc);
        }
    }

    public static class SuperclassFilterProvider extends SimpleFilterProvider {
        @Override
        public PropertyFilter findPropertyFilter(Object filterId, Object valueToFilter) {
            String key = (String) filterId;
            PropertyFilter propertyFilter;
            while (true) {
                propertyFilter = _filtersById.get(key);
                if (propertyFilter != null) {
                    break;
                }
                Class<?> superclass = valueToFilter.getClass().getSuperclass();
                if (superclass == null || superclass == Object.class) {
                    break;
                }
                key = superclass.getName();
            }
            return propertyFilter != null ? propertyFilter : super.findPropertyFilter(filterId, valueToFilter);
        }
    }

}
