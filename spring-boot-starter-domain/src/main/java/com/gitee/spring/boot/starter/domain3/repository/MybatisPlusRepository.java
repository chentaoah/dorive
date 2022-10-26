package com.gitee.spring.boot.starter.domain3.repository;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gitee.spring.domain.core3.api.EntityFactory;
import com.gitee.spring.domain.core3.api.EntityIndex;
import com.gitee.spring.domain.core3.api.Executor;
import com.gitee.spring.domain.core3.entity.definition.ElementDefinition;
import com.gitee.spring.domain.core3.entity.definition.EntityDefinition;
import com.gitee.spring.domain.core3.impl.DefaultEntityFactory;
import com.gitee.spring.domain.core3.repository.AbstractContextRepository;
import com.gitee.spring.domain.core3.repository.ConfiguredRepository;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

public class MybatisPlusRepository<E, PK> extends AbstractContextRepository<E, PK> {

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
        String[] orderBy = null;
        String sort = null;
        if (StringUtils.isNotBlank(orderByAsc)) {
            orderByAsc = StrUtil.toUnderlineCase(orderByAsc);
            orderBy = StrUtil.splitTrim(orderByAsc, ",").toArray(new String[0]);
            sort = "asc";
        }
        if (StringUtils.isNotBlank(orderByDesc)) {
            orderByDesc = StrUtil.toUnderlineCase(orderByDesc);
            orderBy = StrUtil.splitTrim(orderByDesc, ",").toArray(new String[0]);
            sort = "desc";
        }

        Class<?> factoryClass = entityDefinition.getFactory();
        EntityFactory entityFactory;
        if (factoryClass == DefaultEntityFactory.class) {
            entityFactory = new DefaultEntityFactory(elementDefinition);

        } else if (DefaultEntityFactory.class.isAssignableFrom(factoryClass)) {
            DefaultEntityFactory defaultEntityFactory = (DefaultEntityFactory) applicationContext.getBean(factoryClass);
            defaultEntityFactory.setElementDefinition(elementDefinition);
            entityFactory = defaultEntityFactory;

        } else {
            entityFactory = (EntityFactory) applicationContext.getBean(factoryClass);
        }

        MybatisPlusExecutor executor = new MybatisPlusExecutor();
        executor.setElementDefinition(elementDefinition);
        executor.setEntityDefinition(entityDefinition);
        executor.setBaseMapper((BaseMapper<Object>) mapper);
        executor.setPojoClass(pojoClass);
        executor.setOrderBy(orderBy);
        executor.setSort(sort);
        executor.setEntityFactory(entityFactory);
        return executor;
    }

    @Override
    public EntityIndex newEntityIndex(ConfiguredRepository repository, List<Object> entities) {
        return null;
    }

}
