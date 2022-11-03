package com.gitee.spring.domain.core3.impl.executor;

import com.gitee.spring.domain.core3.api.EntityHandler;
import com.gitee.spring.domain.core3.api.Executor;
import com.gitee.spring.domain.core3.entity.BoundedContext;
import com.gitee.spring.domain.core3.impl.resolver.DelegateResolver;
import com.gitee.spring.domain.core3.repository.AbstractContextRepository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AdaptiveExecutor extends ChainExecutor {

    public AdaptiveExecutor(AbstractContextRepository<?, ?> repository, EntityHandler entityHandler) {
        super(repository, entityHandler);
    }

    @Override
    public void handleEntities(BoundedContext boundedContext, List<Object> rootEntities) {
        Map<AbstractContextRepository<?, ?>, List<Object>> repositoryEntitiesMap = collectRepositoryEntitiesMap(rootEntities);
        repositoryEntitiesMap.forEach((repository, entities) -> {
            Executor executor = repository.getExecutor();
            if (executor instanceof ChainExecutor) {
                ((ChainExecutor) executor).doHandleEntities(boundedContext, entities);
            }
        });
    }

    private Map<AbstractContextRepository<?, ?>, List<Object>> collectRepositoryEntitiesMap(List<Object> rootEntities) {
        Map<AbstractContextRepository<?, ?>, List<Object>> repositoryEntitiesMap = new LinkedHashMap<>();
        for (Object rootEntity : rootEntities) {
            DelegateResolver delegateResolver = getRepository().getDelegateResolver();
            AbstractContextRepository<?, ?> abstractContextRepository = delegateResolver.delegateRepository(rootEntity);
            List<Object> entities = repositoryEntitiesMap.computeIfAbsent(abstractContextRepository, key -> new ArrayList<>(rootEntities.size()));
            entities.add(rootEntity);
        }
        return repositoryEntitiesMap;
    }

}
