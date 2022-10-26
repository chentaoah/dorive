package com.gitee.spring.domain.core3.impl.resolver;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.gitee.spring.domain.core.utils.PathUtils;
import com.gitee.spring.domain.core3.api.Binder;
import com.gitee.spring.domain.core3.entity.PropertyChain;
import com.gitee.spring.domain.core3.entity.definition.BindingDefinition;
import com.gitee.spring.domain.core3.entity.definition.ElementDefinition;
import com.gitee.spring.domain.core3.impl.binder.ContextBinder;
import com.gitee.spring.domain.core3.impl.binder.PropertyBinder;
import com.gitee.spring.domain.core3.repository.AbstractContextRepository;
import com.gitee.spring.domain.core3.repository.ConfiguredRepository;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

@Data
public class BinderResolver {

    private AbstractContextRepository<?, ?> repository;

    private List<Binder> allBinders;
    private List<PropertyBinder> propertyBinders;
    private List<ContextBinder> contextBinders;
    private String[] boundColumns;

    private List<Binder> boundValueBinders;
    private PropertyBinder boundIdBinder;

    public BinderResolver(AbstractContextRepository<?, ?> repository) {
        this.repository = repository;
    }

    public void resolveBinders(String accessPath, ElementDefinition elementDefinition) {
        allBinders = new ArrayList<>();
        propertyBinders = new ArrayList<>();
        contextBinders = new ArrayList<>();
        Set<String> boundColumns = new LinkedHashSet<>();

        List<BindingDefinition> bindingDefinitions = BindingDefinition.newBindingDefinitions(elementDefinition);
        for (BindingDefinition bindingDefinition : bindingDefinitions) {
            renewBindingDefinition(accessPath, bindingDefinition);
            if (StringUtils.isNotBlank(bindingDefinition.getBindProp())) {
                PropertyBinder propertyBinder = newPropertyBinder(bindingDefinition);
                allBinders.add(propertyBinder);
                propertyBinders.add(propertyBinder);
                boundColumns.add(StrUtil.toUnderlineCase(bindingDefinition.getAlias()));

            } else {
                ContextBinder contextBinder = newContextBinder(bindingDefinition);
                allBinders.add(contextBinder);
                contextBinders.add(contextBinder);
            }
        }

        this.boundColumns = boundColumns.toArray(new String[0]);
    }

    private void renewBindingDefinition(String accessPath, BindingDefinition bindingDefinition) {
        String field = bindingDefinition.getField();
        String bindProp = bindingDefinition.getBindProp();
        String bindCtx = bindingDefinition.getBindCtx();
        String alias = bindingDefinition.getAlias();
        String bindAlias = bindingDefinition.getBindAlias();

        Assert.notBlank(bindProp + bindCtx, "The bindProp and bindCtx cannot be blank at the same time!");

        if (StringUtils.isBlank(alias)) {
            alias = field;
        }

        if (StringUtils.isNotBlank(bindProp)) {
            if (bindProp.startsWith(".")) {
                bindProp = PathUtils.getAbsolutePath(accessPath, bindProp);
            }
            if (StringUtils.isBlank(bindAlias)) {
                bindAlias = PathUtils.getFieldName(bindProp);
            }
        }

        bindingDefinition.setField(field);
        bindingDefinition.setBindProp(bindProp);
        bindingDefinition.setBindCtx(bindCtx);
        bindingDefinition.setAlias(alias);
        bindingDefinition.setBindAlias(bindAlias);
    }

    private PropertyBinder newPropertyBinder(BindingDefinition bindingDefinition) {
        Map<String, ConfiguredRepository> allRepositoryMap = repository.getAllRepositoryMap();
        String belongAccessPath = PathUtils.getBelongPath(allRepositoryMap.keySet(), bindingDefinition.getBindProp());
        ConfiguredRepository belongRepository = allRepositoryMap.get(belongAccessPath);
        Assert.notNull(belongRepository, "The belong repository cannot be null!");
        belongRepository.setBoundEntity(true);

        Map<String, PropertyChain> propertyChains = repository.getPropertyResolver().getPropertyChains();
        PropertyChain boundPropertyChain = propertyChains.get(bindingDefinition.getBindProp());
        Assert.notNull(boundPropertyChain, "The bound property chain cannot be null!");
        boundPropertyChain.initialize();

        return new PropertyBinder(bindingDefinition, null, belongAccessPath, belongRepository, boundPropertyChain);
    }

    private ContextBinder newContextBinder(BindingDefinition bindingDefinition) {
        return new ContextBinder(bindingDefinition, null);
    }

}
