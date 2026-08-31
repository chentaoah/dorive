package com.gitee.dorive.base.v1.executor.impl.selector;

import com.gitee.dorive.base.v1.executor.api.Matcher;
import com.gitee.dorive.base.v1.executor.api.Selection;
import com.gitee.dorive.base.v1.executor.api.Selector;
import com.gitee.dorive.base.v1.executor.impl.matcher.AllMatcher;
import com.gitee.dorive.base.v1.executor.impl.matcher.NoneMatcher;
import com.gitee.dorive.base.v1.executor.impl.matcher.RootMatcher;
import com.gitee.dorive.base.v1.repository.api.RepositoryItem;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DefaultSelector implements Selector {

    public static final Selector NONE_SELECTOR = new DefaultSelector(new NoneMatcher());
    public static final Selector ROOT_SELECTOR = new DefaultSelector(new RootMatcher());
    public static final Selector ALL_SELECTOR = new DefaultSelector(new AllMatcher());

    private Matcher matcher;
    private List<Selection> selections;

    public DefaultSelector(Matcher matcher) {
        this.matcher = matcher;
    }

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
