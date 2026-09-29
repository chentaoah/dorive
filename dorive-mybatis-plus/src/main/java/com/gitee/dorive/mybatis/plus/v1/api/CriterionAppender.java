/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.mybatis.plus.v1.api;

import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
import com.gitee.dorive.base.v1.executor.entity.qry.Example;

public interface CriterionAppender {

    void appendCriterion(AbstractWrapper<?, String, ?> wrapper, Example example, String property, Object value);

}
