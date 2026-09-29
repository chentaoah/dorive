/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.executor.v1.entity;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import lombok.Getter;
import lombok.Setter;

import java.util.Collections;
import java.util.List;

@Getter
@Setter
public class Selection {

    private List<String> properties;

    public Selection(String propText) {
        Assert.notBlank(propText, "The propText cannot be blank!");
        this.properties = Collections.unmodifiableList(StrUtil.splitTrim(propText, ","));
    }
}
