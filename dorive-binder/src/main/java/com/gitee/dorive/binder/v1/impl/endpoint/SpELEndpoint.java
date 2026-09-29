/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.binder.v1.impl.endpoint;

import com.gitee.dorive.base.v1.definition.entity.FieldDefinition;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

public class SpELEndpoint extends AbstractEndpoint {

    private final Expression expression;

    public SpELEndpoint(FieldDefinition fieldDefinition, String expression) {
        super(fieldDefinition);
        this.expression = new SpelExpressionParser().parseExpression(expression);
    }

    @Override
    public Object getValue(Object entity) {
        EvaluationContext context = new StandardEvaluationContext();
        context.setVariable("entity", entity);
        return expression.getValue(context, Object.class);
    }

    @Override
    public void setValue(Object entity, Object value) {
        EvaluationContext context = new StandardEvaluationContext();
        context.setVariable("entity", entity);
        expression.setValue(context, value);
    }

}
