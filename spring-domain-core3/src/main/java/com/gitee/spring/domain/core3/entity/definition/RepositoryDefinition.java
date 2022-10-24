package com.gitee.spring.domain.core3.entity.definition;

import com.gitee.spring.domain.core3.api.Binder;
import com.gitee.spring.domain.core3.impl.binder.ContextBinder;
import com.gitee.spring.domain.core3.impl.binder.PropertyBinder;
import com.gitee.spring.domain.core3.repository.AbstractRepository;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class RepositoryDefinition {
    private boolean aggregateRoot;
    private String accessPath;
    private List<Binder> allBinders;
    private List<PropertyBinder> propertyBinders;
    private List<ContextBinder> contextBinders;
    private List<Binder> boundValueBinders;
    private PropertyBinder boundIdBinder;
    private String[] boundColumns;
    private AbstractRepository<?, ?> abstractRepository;
}
