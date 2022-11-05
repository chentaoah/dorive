package com.gitee.spring.domain.core.impl.resolver;

import cn.hutool.core.lang.Assert;
import com.gitee.spring.domain.core.api.Binder;
import com.gitee.spring.domain.core.entity.PropertyChain;
import com.gitee.spring.domain.core.entity.definition.BindingDefinition;
import com.gitee.spring.domain.core.entity.definition.EntityDefinition;
import com.gitee.spring.domain.core.impl.binder.AbstractBinder;
import com.gitee.spring.domain.core.impl.binder.ContextBinder;
import com.gitee.spring.domain.core.impl.binder.PropertyBinder;
import com.gitee.spring.domain.core.repository.AbstractContextRepository;
import com.gitee.spring.domain.core.repository.ConfiguredRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RepoBinderResolver {

    private final AbstractContextRepository<?, ?> repository;

    public RepoBinderResolver(AbstractContextRepository<?, ?> repository) {
        this.repository = repository;
    }

    public void resolveValueBinders() {
        Map<String, ConfiguredRepository> allRepositoryMap = repository.getAllRepositoryMap();
        allRepositoryMap.forEach((accessPath, repository) -> {

            BinderResolver binderResolver = repository.getBinderResolver();
            Map<String, PropertyChain> propertyChainMap = repository.getPropertyChainMap();

            List<Binder> boundValueBinders = new ArrayList<>();
            PropertyBinder boundIdBinder = null;

            for (Binder binder : binderResolver.getAllBinders()) {
                BindingDefinition bindingDefinition = binder.getBindingDefinition();
                String field = bindingDefinition.getField();

                if (binder instanceof AbstractBinder) {
                    String fieldAccessPath = repository.getFieldPrefix() + field;
                    PropertyChain fieldPropertyChain = propertyChainMap.get(fieldAccessPath);
                    Assert.notNull(fieldPropertyChain, "The field property chain cannot be null!");
                    fieldPropertyChain.initialize();
                    ((AbstractBinder) binder).setFieldPropertyChain(fieldPropertyChain);
                }

                if (binder instanceof PropertyBinder) {
                    PropertyBinder propertyBinder = (PropertyBinder) binder;
                    if (isSameType(propertyBinder)) {
                        if (!"id".equals(field)) {
                            boundValueBinders.add(propertyBinder);
                        } else {
                            EntityDefinition entityDefinition = repository.getEntityDefinition();
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
        PropertyChain fieldPropertyChain = propertyBinder.getFieldPropertyChain();
        PropertyChain boundPropertyChain = propertyBinder.getBoundPropertyChain();
        return fieldPropertyChain.getProperty().isSameType(boundPropertyChain.getProperty());
    }

}
