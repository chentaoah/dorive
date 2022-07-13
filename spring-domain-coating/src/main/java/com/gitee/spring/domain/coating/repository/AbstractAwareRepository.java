package com.gitee.spring.domain.coating.repository;

import cn.hutool.core.util.StrUtil;
import com.gitee.spring.domain.core.entity.EntityDefinition;
import com.gitee.spring.domain.coating.entity.RepositoryDefinition;
import com.gitee.spring.domain.core.repository.AbstractRepository;
import com.gitee.spring.domain.core.repository.ConfiguredRepository;
import com.gitee.spring.domain.event.repository.AbstractEventRepository;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.*;

@Data
@EqualsAndHashCode(callSuper = false)
public abstract class AbstractAwareRepository<E, PK> extends AbstractEventRepository<E, PK> {

    protected Map<String, RepositoryDefinition> repositoryDefinitionMap = new LinkedHashMap<>();

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        RepositoryDefinition rootRepositoryDefinition = new RepositoryDefinition(
                "",
                "/",
                false,
                rootRepository,
                rootRepository);
        repositoryDefinitionMap.put("/", rootRepositoryDefinition);
        resolveRepositoryDefinitionMap(new ArrayList<>(), this);
    }

    protected void resolveRepositoryDefinitionMap(List<String> multiAccessPath, AbstractAwareRepository<?, ?> abstractAwareRepository) {
        String prefixAccessPath = StrUtil.join("", multiAccessPath);

        for (ConfiguredRepository configuredRepository : abstractAwareRepository.getSubRepositories()) {
            EntityDefinition entityDefinition = configuredRepository.getEntityDefinition();
            String absoluteAccessPath = prefixAccessPath + entityDefinition.getAccessPath();

            AbstractRepository<Object, Object> abstractRepository = configuredRepository.getProxyRepository();
            if (abstractRepository instanceof AbstractAwareRepository) {
                AbstractAwareRepository<?, ?> repository = (AbstractAwareRepository<?, ?>) abstractRepository;

                RepositoryDefinition repositoryDefinition = new RepositoryDefinition(
                        prefixAccessPath,
                        absoluteAccessPath,
                        true,
                        configuredRepository,
                        repository.getRootRepository());
                repositoryDefinitionMap.put(absoluteAccessPath, repositoryDefinition);

                multiAccessPath = new ArrayList<>(multiAccessPath);
                multiAccessPath.add(entityDefinition.getAccessPath());
                resolveRepositoryDefinitionMap(multiAccessPath, repository);

            } else {
                RepositoryDefinition repositoryDefinition = new RepositoryDefinition(
                        prefixAccessPath,
                        absoluteAccessPath,
                        false,
                        configuredRepository,
                        configuredRepository);
                repositoryDefinitionMap.put(absoluteAccessPath, repositoryDefinition);
            }
        }
    }

}
