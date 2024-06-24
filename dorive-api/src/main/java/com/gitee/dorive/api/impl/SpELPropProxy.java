package com.gitee.dorive.api.impl;

import com.gitee.dorive.api.api.PropProxy;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

@Data
@AllArgsConstructor
public class SpELPropProxy implements PropProxy {

    private final Expression expression;

    public static PropProxy newPropProxy(String expression) {
        ExpressionParser parser = new SpelExpressionParser();
        return new SpELPropProxy(parser.parseExpression(expression));
    }

    @Override
    public Object getValue(Object entity) {
        EvaluationContext context = new StandardEvaluationContext();
        context.setVariable("root", entity);
        return expression.getValue(context);
    }

    @Override
    public void setValue(Object entity, Object value) {
        EvaluationContext context = new StandardEvaluationContext();
        context.setVariable("root", entity);
        expression.setValue(context, value);
    }

}
