/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.executor.v1.impl.index;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.gitee.dorive.base.v1.repository.api.RepositoryItem;
import com.gitee.dorive.executor.v1.api.IndexProvider;
import com.gitee.dorive.executor.v1.entity.Selection;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class NameIndexProvider implements IndexProvider {

    private List<String> names;
    private List<Selection> selections;

    public NameIndexProvider(String... strings) {
        Assert.notEmpty(strings, "The strings cannot be empty!");
        List<String> names = new ArrayList<>(strings.length);
        List<Selection> selections = new ArrayList<>(strings.length);
        for (String str : strings) {
            String name = str;
            Selection selection = null;
            if (str.contains("(") && str.contains(")")) {
                name = StrUtil.subBefore(str, "(", false);
                selection = new Selection(StrUtil.subBetween(str, "(", ")"));
            }
            names.add(name);
            selections.add(selection);
        }
        this.names = Collections.unmodifiableList(names);
        this.selections = Collections.unmodifiableList(selections);
    }

    @Override
    public int indexOf(RepositoryItem repositoryItem) {
        return names.indexOf(repositoryItem.getName());
    }
}

