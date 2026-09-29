/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.executor.v1.impl.selector;

import com.gitee.dorive.base.v1.executor.api.Selector;
import com.gitee.dorive.base.v1.repository.api.RepositoryItem;
import com.gitee.dorive.executor.v1.api.IndexProvider;
import com.gitee.dorive.executor.v1.entity.Selection;
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

    private IndexProvider indexProvider;
    private List<Selection> selections;

    @Override
    public boolean matches(RepositoryItem repositoryItem) {
        return indexProvider != null && indexProvider.indexOf(repositoryItem) >= 0;
    }

    @Override
    public List<String> select(RepositoryItem repositoryItem) {
        if (indexProvider != null && selections != null) {
            int index = indexProvider.indexOf(repositoryItem);
            if (index >= 0 && index < selections.size()) {
                Selection selection = selections.get(index);
                if (selection != null) {
                    return selection.getProperties();
                }
            }
        }
        return null;
    }

}
