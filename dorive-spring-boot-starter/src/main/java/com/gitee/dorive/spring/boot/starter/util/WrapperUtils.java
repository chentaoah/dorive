/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gitee.dorive.spring.boot.starter.util;

import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
import com.gitee.dorive.core.entity.executor.Criterion;
import com.gitee.dorive.core.entity.executor.Example;
import com.gitee.dorive.spring.boot.starter.api.CriterionAppender;

import static com.gitee.dorive.spring.boot.starter.impl.AppenderContext.OPERATOR_CRITERION_APPENDER_MAP;

public class WrapperUtils {

    public static void appendCriterion(AbstractWrapper<?, String, ?> abstractWrapper, Example example) {
        for (Criterion criterion : example.getCriteria()) {
            CriterionAppender criterionAppender = OPERATOR_CRITERION_APPENDER_MAP.get(criterion.getOperator());
            criterionAppender.appendCriterion(abstractWrapper, criterion.getProperty(), criterion.getValue());
        }
    }

}