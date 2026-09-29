/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.base.v1.executor.entity.qry;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class UnionExample extends Example {

    private List<Example> examples = new ArrayList<>();

    @Override
    public boolean isEmpty() {
        return examples.isEmpty();
    }

    @Override
    public boolean isNotEmpty() {
        return !examples.isEmpty();
    }

    public void addExample(Example example) {
        examples.add(example);
    }

}
