package com.gitee.dorive.core.impl.observe;

import com.gitee.dorive.core.api.Observed;
import com.gitee.dorive.core.entity.BoundedContext;
import com.gitee.dorive.core.repository.CommonRepository;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Collection;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ObservedList<E> extends ArrayList<E> implements Observed {

    private Observed observed;

    public ObservedList(Collection<? extends E> c, Observed observed) {
        super(c);
        this.observed = observed;
    }

    @Override
    public ObservedResult accept(CommonRepository repository, BoundedContext boundedContext, Object entity) {
        return observed.accept(repository, boundedContext, entity);
    }

}
