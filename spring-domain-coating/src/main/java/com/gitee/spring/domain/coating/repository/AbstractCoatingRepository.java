package com.gitee.spring.domain.coating.repository;

import com.gitee.spring.domain.coating.annotation.CoatingScan;
import com.gitee.spring.domain.coating.api.CoatingRepository;
import com.gitee.spring.domain.coating.api.ExampleBuilder;
import com.gitee.spring.domain.coating.impl.resolver.CoatingWrapperResolver;
import com.gitee.spring.domain.coating.impl.DefaultExampleBuilder;
import com.gitee.spring.domain.coating.impl.resolver.RepoDefinitionResolver;
import com.gitee.spring.domain.core.entity.BoundedContext;
import com.gitee.spring.domain.core.entity.executor.Example;
import com.gitee.spring.domain.core.entity.executor.Page;
import com.gitee.spring.domain.event.repository.AbstractEventRepository;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public abstract class AbstractCoatingRepository<E, PK> extends AbstractEventRepository<E, PK> implements ExampleBuilder, CoatingRepository<E, PK> {

    protected RepoDefinitionResolver repoDefinitionResolver = new RepoDefinitionResolver(this);
    protected CoatingWrapperResolver coatingWrapperResolver = new CoatingWrapperResolver(this);
    protected ExampleBuilder exampleBuilder = new DefaultExampleBuilder(this);

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        CoatingScan coatingScan = AnnotatedElementUtils.getMergedAnnotation(this.getClass(), CoatingScan.class);
        if (coatingScan != null) {
            repoDefinitionResolver.resolveRepositoryDefinitionMap();
            coatingWrapperResolver.resolveCoatingWrapperMap(coatingScan.value());
        }
    }

    @Override
    public Example buildExample(BoundedContext boundedContext, Object coatingObject) {
        return exampleBuilder.buildExample(boundedContext, coatingObject);
    }

    @Override
    public List<E> selectByCoating(BoundedContext boundedContext, Object coatingObject) {
        Example example = buildExample(boundedContext, coatingObject);
        return selectByExample(boundedContext, example);
    }

    @Override
    public Page<E> selectPageByCoating(BoundedContext boundedContext, Object coatingObject) {
        Example example = buildExample(boundedContext, coatingObject);
        return selectPageByExample(boundedContext, example);
    }

}
