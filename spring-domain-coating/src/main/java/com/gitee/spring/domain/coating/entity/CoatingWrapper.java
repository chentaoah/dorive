package com.gitee.spring.domain.coating.entity;

import cn.hutool.core.convert.Convert;
import com.gitee.spring.domain.coating.entity.definition.CoatingDefinition;
import com.gitee.spring.domain.core.entity.executor.Page;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class CoatingWrapper {

    private CoatingDefinition coatingDefinition;
    private List<RepositoryWrapper> repositoryWrappers;
    private List<RepositoryWrapper> reversedRepositoryWrappers;
    private PropertyWrapper pageNumPropertyWrapper;
    private PropertyWrapper pageSizePropertyWrapper;
    
    public Page<Object> getPageInfo(Object object) {
        if (pageNumPropertyWrapper != null && pageSizePropertyWrapper != null) {
            Object pageNum = pageNumPropertyWrapper.getProperty().getFieldValue(object);
            Object pageSize = pageSizePropertyWrapper.getProperty().getFieldValue(object);
            if (pageNum != null && pageSize != null) {
                return new Page<>(Convert.convert(Long.class, pageNum), Convert.convert(Long.class, pageSize));
            }
        }
        return null;
    }

}
