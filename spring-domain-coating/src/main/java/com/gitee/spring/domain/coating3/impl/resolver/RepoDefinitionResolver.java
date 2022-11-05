package com.gitee.spring.domain.coating3.impl.resolver;

import cn.hutool.core.util.StrUtil;
import com.gitee.spring.domain.coating3.entity.PropertyWrapper;
import com.gitee.spring.domain.coating3.entity.RepositoryWrapper;
import com.gitee.spring.domain.coating3.entity.definition.RepositoryDefinition;
import com.gitee.spring.domain.core3.entity.definition.ElementDefinition;
import com.gitee.spring.domain.core3.repository.AbstractContextRepository;
import com.gitee.spring.domain.core3.repository.AbstractRepository;
import com.gitee.spring.domain.core3.repository.ConfiguredRepository;
import lombok.Data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
public class RepoDefinitionResolver {

    private AbstractContextRepository<?, ?> repository;

    private Map<String, RepositoryDefinition> repositoryDefinitionMap = new LinkedHashMap<>();

    public RepoDefinitionResolver(AbstractContextRepository<?, ?> repository) {
        this.repository = repository;
    }

    public void resolveRepositoryDefinitionMap() {
        RepositoryDefinition repositoryDefinition = new RepositoryDefinition(
                "",
                "/",
                false,
                repository.getRootRepository(),
                repository.getRootRepository());
        repositoryDefinitionMap.put("/", repositoryDefinition);
        resolveRepositoryDefinitionMap(new ArrayList<>(), repository);
    }

    private void resolveRepositoryDefinitionMap(List<String> multiAccessPath, AbstractContextRepository<?, ?> repository) {
        String prefixAccessPath = StrUtil.join("", multiAccessPath);

        for (ConfiguredRepository subRepository : repository.getSubRepositories()) {
            String accessPath = subRepository.getAccessPath();
            String absoluteAccessPath = prefixAccessPath + accessPath;

            AbstractRepository<Object, Object> abstractRepository = subRepository.getProxyRepository();
            if (abstractRepository instanceof AbstractContextRepository) {
                AbstractContextRepository<?, ?> abstractContextRepository = (AbstractContextRepository<?, ?>) abstractRepository;

                RepositoryDefinition repositoryDefinition = new RepositoryDefinition(
                        prefixAccessPath,
                        absoluteAccessPath,
                        true,
                        subRepository,
                        abstractContextRepository.getRootRepository());
                repositoryDefinitionMap.put(absoluteAccessPath, repositoryDefinition);

                List<String> newMultiAccessPath = new ArrayList<>(multiAccessPath);
                newMultiAccessPath.add(accessPath);
                resolveRepositoryDefinitionMap(newMultiAccessPath, abstractContextRepository);

            } else {
                RepositoryDefinition repositoryDefinition = new RepositoryDefinition(
                        prefixAccessPath,
                        absoluteAccessPath,
                        false,
                        subRepository,
                        subRepository);
                repositoryDefinitionMap.put(absoluteAccessPath, repositoryDefinition);
            }
        }
    }

    public List<RepositoryWrapper> collectRepositoryWrappers(Map<String, List<PropertyWrapper>> locationPropertyWrappersMap,
                                                             Map<String, PropertyWrapper> fieldPropertyWrapperMap) {
        List<RepositoryWrapper> repositoryWrappers = new ArrayList<>();

        for (RepositoryDefinition repositoryDefinition : repositoryDefinitionMap.values()) {
            String absoluteAccessPath = repositoryDefinition.getAbsoluteAccessPath();
            ConfiguredRepository repository = repositoryDefinition.getConfiguredRepository();
            ElementDefinition elementDefinition = repository.getElementDefinition();

            List<PropertyWrapper> propertyWrappers = new ArrayList<>();

            List<PropertyWrapper> locationPropertyWrappers = locationPropertyWrappersMap.get(absoluteAccessPath);
            if (locationPropertyWrappers != null) {
                propertyWrappers.addAll(locationPropertyWrappers);
            }

            for (String fieldName : elementDefinition.getProperties()) {
                PropertyWrapper propertyWrapper = fieldPropertyWrapperMap.get(fieldName);
                if (propertyWrapper != null) {
                    propertyWrappers.add(propertyWrapper);
                }
            }

            if (!propertyWrappers.isEmpty() || repository.isBoundEntity()) {
                RepositoryWrapper repositoryWrapper = new RepositoryWrapper(repositoryDefinition, propertyWrappers);
                repositoryWrappers.add(repositoryWrapper);
            }
        }

        return repositoryWrappers;
    }

}
