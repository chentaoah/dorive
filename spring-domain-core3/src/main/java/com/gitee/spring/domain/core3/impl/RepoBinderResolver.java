package com.gitee.spring.domain.core3.impl;

import cn.hutool.core.lang.Assert;
import com.gitee.spring.domain.core.entity.EntityPropertyChain;
import com.gitee.spring.domain.core3.api.Binder;
import com.gitee.spring.domain.core3.entity.definition.BindingDefinition;
import com.gitee.spring.domain.core3.entity.definition.EntityDefinition;
import com.gitee.spring.domain.core3.impl.binder.AbstractBinder;
import com.gitee.spring.domain.core3.impl.binder.ContextBinder;
import com.gitee.spring.domain.core3.impl.binder.PropertyBinder;
import com.gitee.spring.domain.core3.repository.AbstractContextRepository;
import com.gitee.spring.domain.core3.repository.ConfiguredRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RepoBinderResolver {

    private final AbstractContextRepository<?, ?> repository;

    public RepoBinderResolver(AbstractContextRepository<?, ?> repository) {
        this.repository = repository;
    }

    public void resolveBinders() {
        Map<String, ConfiguredRepository> allRepositoryMap = repository.getAllRepositoryMap();
        allRepositoryMap.forEach((accessPath, configuredRepository) -> {

            String prefixAccessPath = configuredRepository.isAggregateRoot() ? "/" : configuredRepository.getAccessPath() + "/";
            Map<String, EntityPropertyChain> properties = configuredRepository.getProperties();

            if (properties.isEmpty() && configuredRepository.getElementDefinition().isCollection()) {
                PropertyResolver propertyResolver = new PropertyResolver();
                propertyResolver.resolveProperties("", configuredRepository.getElementDefinition().getGenericEntityClass());
                Map<String, EntityPropertyChain> subAllEntityPropertyChainMap = propertyResolver.getProperties();
                properties.putAll(subAllEntityPropertyChainMap);
                prefixAccessPath = "/";
            }

            BinderResolver binderResolver = configuredRepository.getBinderResolver();

            List<Binder> boundValueBinders = new ArrayList<>();
            PropertyBinder boundIdBinder = null;
            for (Binder binder : binderResolver.getAllBinders()) {

                BindingDefinition bindingDefinition = binder.getBindingDefinition();
                String field = bindingDefinition.getField();

                if (binder instanceof AbstractBinder) {
                    String fieldAccessPath = prefixAccessPath + field;
                    EntityPropertyChain entityPropertyChain = properties.get(fieldAccessPath);
                    Assert.notNull(entityPropertyChain, "The field entity property cannot be null!");
                    entityPropertyChain.initialize();
                    ((AbstractBinder) binder).setFieldProperty(entityPropertyChain);
                }

                if (binder instanceof PropertyBinder) {
                    PropertyBinder propertyBinder = (PropertyBinder) binder;
                    if (isSameType(propertyBinder)) {
                        if (!"id".equals(field)) {
                            boundValueBinders.add(propertyBinder);
                        } else {
                            EntityDefinition entityDefinition = configuredRepository.getEntityDefinition();
                            if (entityDefinition.getOrder() == 0) {
                                entityDefinition.setOrder(-1);
                            }
                            boundIdBinder = propertyBinder;
                        }
                    }

                } else if (binder instanceof ContextBinder) {
                    boundValueBinders.add(binder);
                }
            }

            binderResolver.setBoundValueBinders(boundValueBinders);
            binderResolver.setBoundIdBinder(boundIdBinder);
        });
    }

    private boolean isSameType(PropertyBinder propertyBinder) {
        return false;
    }

}
