package com.gitee.spring.domain.core.repository;

import com.gitee.spring.domain.core.api.MetadataGetter;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class DefaultRepository extends AbstractRepository<Object, Object> implements MetadataGetter {

    @Override
    public Object getMetadata() {
        if (executor instanceof MetadataGetter) {
            return ((MetadataGetter) executor).getMetadata();
        }
        return null;
    }

}
