package com.gitee.dorive.executor.v1.impl.selector;

import com.gitee.dorive.base.v1.executor.api.Matcher;
import com.gitee.dorive.base.v1.executor.api.Selection;
import com.gitee.dorive.base.v1.executor.api.Selector;
import com.gitee.dorive.base.v1.repository.api.RepositoryItem;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class DefaultSelector implements Selector {

    private Matcher matcher;
    private List<Selection> selections;

    @Override
    public boolean matches(RepositoryItem repositoryItem) {
        return matcher != null && matcher.matches(repositoryItem);
    }

    @Override
    public List<String> select(RepositoryItem repositoryItem) {
        if (matcher != null && selections != null) {
            int index = matcher.indexOf(repositoryItem);
            if (index >= 0 && index < selections.size()) {
                Selection selection = selections.get(index);
                if (selection != null) {
                    return selection.select();
                }
            }
        }
        return null;
    }

}
