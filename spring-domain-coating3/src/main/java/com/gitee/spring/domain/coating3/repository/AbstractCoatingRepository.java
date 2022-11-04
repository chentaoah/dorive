package com.gitee.spring.domain.coating3.repository;

import com.gitee.spring.domain.coating3.annotation.EnableCoating;
import com.gitee.spring.domain.coating3.api.ExampleBuilder;
import com.gitee.spring.domain.coating3.impl.resolver.CoatingWrapperResolver;
import com.gitee.spring.domain.coating3.impl.DefaultExampleBuilder;
import com.gitee.spring.domain.coating3.impl.resolver.RepoDefinitionResolver;
import com.gitee.spring.domain.core3.entity.BoundedContext;
import com.gitee.spring.domain.core3.entity.executor.Example;
import com.gitee.spring.domain.event3.repository.AbstractEventRepository;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.core.annotation.AnnotatedElementUtils;

@Data
@EqualsAndHashCode(callSuper = false)
public abstract class AbstractCoatingRepository<E, PK> extends AbstractEventRepository<E, PK> implements ExampleBuilder {

    protected RepoDefinitionResolver repoDefinitionResolver = new RepoDefinitionResolver(this);
    protected CoatingWrapperResolver coatingWrapperResolver = new CoatingWrapperResolver(this);
    protected ExampleBuilder exampleBuilder = new DefaultExampleBuilder(this);

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        EnableCoating enableCoating = AnnotatedElementUtils.getMergedAnnotation(this.getClass(), EnableCoating.class);
        if (enableCoating != null) {
            repoDefinitionResolver.resolveRepositoryDefinitionMap();
            coatingWrapperResolver.resolveCoatingWrapperMap(enableCoating.value());
        }
    }

    @Override
    public Example buildExample(BoundedContext boundedContext, Object coatingObject) {
        return exampleBuilder.buildExample(boundedContext, coatingObject);
    }

}
