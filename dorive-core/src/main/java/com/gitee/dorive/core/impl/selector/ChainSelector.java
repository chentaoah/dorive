package com.gitee.dorive.core.impl.selector;

import com.gitee.dorive.core.api.Selector;
import com.gitee.dorive.core.entity.BoundedContext;
import com.gitee.dorive.core.repository.CommonRepository;
import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class ChainSelector extends NameSelector {

    private final Selector selector;

    public ChainSelector(Selector selector, String... names) {
        super(names);
        this.selector = selector;
    }

    @Override
    public boolean isMatch(BoundedContext boundedContext, CommonRepository repository) {
        return super.isMatch(boundedContext, repository) || selector.isMatch(boundedContext, repository);
    }

    @Override
    public List<String> selectColumns(BoundedContext boundedContext, CommonRepository repository) {
        List<String> columns = super.selectColumns(boundedContext, repository);
        if (columns == null || columns.isEmpty()) {
            return selector.selectColumns(boundedContext, repository);
        }
        return columns;
    }

}
