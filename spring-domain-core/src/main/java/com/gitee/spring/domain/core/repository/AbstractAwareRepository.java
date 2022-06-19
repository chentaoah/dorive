package com.gitee.spring.domain.core.repository;

import cn.hutool.core.util.StrUtil;
import com.gitee.spring.domain.core.entity.EntityDefinition;
import com.gitee.spring.domain.core.entity.RepositoryGroup;
import com.gitee.spring.domain.core.entity.RepositoryDefinition;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

@Data
@EqualsAndHashCode(callSuper = false)
public abstract class AbstractAwareRepository<E, PK> extends AbstractDelegateRepository<E, PK> {

    protected Map<String, RepositoryGroup> repositoryGroupMap = new LinkedHashMap<>();
    protected List<RepositoryGroup> repositoryGroups = new ArrayList<>();
    protected List<RepositoryGroup> reversedRepositoryGroups = new ArrayList<>();

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        resolveRepositoryGroups(new ArrayList<>(), null, this);
        repositoryGroups.addAll(repositoryGroupMap.values());
        reversedRepositoryGroups.addAll(repositoryGroupMap.values());
        Collections.reverse(reversedRepositoryGroups);
    }

    protected void resolveRepositoryGroups(List<String> multiAccessPath,
                                           ConfiguredRepository parentConfiguredRepository,
                                           AbstractAwareRepository<?, ?> abstractAwareRepository) {

        List<String> finalMultiAccessPath = multiAccessPath;
        String parentAccessPath = multiAccessPath.size() > 1 ? StrUtil.join("", multiAccessPath.subList(0, multiAccessPath.size() - 1)) : "";
        String prefixAccessPath = StrUtil.join("", multiAccessPath);

        String groupAccessPath = StringUtils.isNotBlank(prefixAccessPath) ? prefixAccessPath : "/";
        RepositoryGroup repositoryGroup = new RepositoryGroup(groupAccessPath, new ArrayList<>());
        repositoryGroupMap.put(groupAccessPath, repositoryGroup);
        List<RepositoryDefinition> repositoryDefinitions = repositoryGroup.getRepositoryDefinitions();

        List<ConfiguredRepository> subRepositories = abstractAwareRepository.getSubRepositories();
        for (ConfiguredRepository configuredRepository : subRepositories) {
            EntityDefinition entityDefinition = configuredRepository.getEntityDefinition();
            String absoluteAccessPath = prefixAccessPath + entityDefinition.getAccessPath();

            AbstractRepository<Object, Object> abstractRepository = configuredRepository.getRepository();
            if (abstractRepository instanceof AbstractAwareRepository) {
                AbstractAwareRepository<?, ?> repository = (AbstractAwareRepository<?, ?>) abstractRepository;

                RepositoryDefinition repositoryDefinition = new RepositoryDefinition(
                        finalMultiAccessPath,
                        parentAccessPath,
                        prefixAccessPath,
                        absoluteAccessPath,
                        true,
                        configuredRepository,
                        repository,
                        repository.getRootRepository());
                repositoryDefinitions.add(repositoryDefinition);

                multiAccessPath = new ArrayList<>(multiAccessPath);
                multiAccessPath.add(entityDefinition.getAccessPath());
                resolveRepositoryGroups(multiAccessPath, configuredRepository, repository);
            } else {
                RepositoryDefinition repositoryDefinition = new RepositoryDefinition(
                        finalMultiAccessPath,
                        parentAccessPath,
                        prefixAccessPath,
                        absoluteAccessPath,
                        false,
                        parentConfiguredRepository,
                        abstractAwareRepository,
                        configuredRepository);
                repositoryDefinitions.add(repositoryDefinition);
            }
        }
    }

    protected AbstractAwareRepository<?, ?> adaptiveRepository(Object rootEntity) {
        return this;
    }

}
