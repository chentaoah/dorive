package com.gitee.spring.boot.starter.domain.repository;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gitee.spring.domain.coating.repository.AbstractCoatingRepository;
import com.gitee.spring.domain.core.api.EntityFactory;
import com.gitee.spring.domain.core.api.Executor;
import com.gitee.spring.domain.core.entity.definition.ElementDefinition;
import com.gitee.spring.domain.core.entity.definition.EntityDefinition;
import com.gitee.spring.domain.core.entity.executor.OrderBy;
import com.gitee.spring.domain.core.impl.DefaultEntityFactory;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class MybatisPlusRepository<E, PK> extends AbstractCoatingRepository<E, PK> {

    @Override
    @SuppressWarnings("unchecked")
    protected Executor newExecutor(ElementDefinition elementDefinition, EntityDefinition entityDefinition) {
        Class<?> mapperClass = entityDefinition.getMapper();
        Object mapper = null;
        Class<?> pojoClass = null;
        if (mapperClass != Object.class) {
            mapper = applicationContext.getBean(mapperClass);
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

        String orderByAsc = entityDefinition.getOrderByAsc();
        String orderByDesc = entityDefinition.getOrderByDesc();
        OrderBy orderBy = null;
        if (StringUtils.isNotBlank(orderByAsc)) {
            orderByAsc = StrUtil.toUnderlineCase(orderByAsc);
            orderBy = new OrderBy(StrUtil.splitTrim(orderByAsc, ",").toArray(new String[0]), "asc");

        } else if (StringUtils.isNotBlank(orderByDesc)) {
            orderByDesc = StrUtil.toUnderlineCase(orderByDesc);
            orderBy = new OrderBy(StrUtil.splitTrim(orderByDesc, ",").toArray(new String[0]), "desc");
        }

        Class<?> factoryClass = entityDefinition.getFactory();
        EntityFactory entityFactory;
        if (factoryClass == DefaultEntityFactory.class) {
            entityFactory = new DefaultEntityFactory(elementDefinition, pojoClass);

        } else if (DefaultEntityFactory.class.isAssignableFrom(factoryClass)) {
            DefaultEntityFactory defaultEntityFactory = (DefaultEntityFactory) applicationContext.getBean(factoryClass);
            defaultEntityFactory.setElementDefinition(elementDefinition);
            defaultEntityFactory.setPojoClass(pojoClass);
            entityFactory = defaultEntityFactory;

        } else {
            entityFactory = (EntityFactory) applicationContext.getBean(factoryClass);
        }

        MybatisPlusExecutor mybatisPlusExecutor = new MybatisPlusExecutor();
        mybatisPlusExecutor.setElementDefinition(elementDefinition);
        mybatisPlusExecutor.setEntityDefinition(entityDefinition);
        mybatisPlusExecutor.setBaseMapper((BaseMapper<Object>) mapper);
        mybatisPlusExecutor.setPojoClass((Class<Object>) pojoClass);
        mybatisPlusExecutor.setOrderBy(orderBy);
        mybatisPlusExecutor.setEntityFactory(entityFactory);
        return mybatisPlusExecutor;
    }

}
