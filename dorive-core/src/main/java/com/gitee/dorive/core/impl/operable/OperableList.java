package com.gitee.dorive.core.impl.operable;

import com.gitee.dorive.core.api.Operable;
import com.gitee.dorive.core.entity.BoundedContext;
import com.gitee.dorive.core.repository.ConfiguredRepository;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Collection;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class OperableList<E> extends ArrayList<E> implements Operable {

    private Operable operable;

    public OperableList(Collection<? extends E> c, Operable operable) {
        super(c);
        this.operable = operable;
    }

    @Override
    public OperationResult accept(ConfiguredRepository repository, BoundedContext boundedContext, Object entity) {
        return operable.accept(repository, boundedContext, entity);
    }

}
