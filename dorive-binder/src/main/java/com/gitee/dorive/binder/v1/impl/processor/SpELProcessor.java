/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.binder.v1.impl.processor;

import com.gitee.dorive.base.v1.definition.def.BindingDef;
import com.gitee.dorive.base.v1.binder.api.Processor;
import com.gitee.dorive.base.v1.executor.api.Context;
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
