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

package com.gitee.dorive.core.impl.processor;

import com.gitee.dorive.api.entity.core.def.BindingDef;
import com.gitee.dorive.core.api.binder.Processor;
import com.gitee.dorive.core.api.context.Context;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

public class SpELProcessor implements Processor {

    private final Expression expression;

    public SpELProcessor(BindingDef bindingDef) {
        ExpressionParser parser = new SpelExpressionParser();
        this.expression = parser.parseExpression(bindingDef.getExpression());
    }

    @Override
    public Object input(Context context, Object value) {
        EvaluationContext evaluationContext = new StandardEvaluationContext();
        evaluationContext.setVariable("ctx", context.getAttachments());
        evaluationContext.setVariable("val", value);
        return expression.getValue(evaluationContext);
    }

    @Override
    public Object output(Context context, Object value) {
        return value;
    }

}
