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

package com.gitee.dorive.core.impl.endpoint;

import com.gitee.dorive.api.entity.ele.FieldElement;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

public class SpELEndpoint extends AbstractEndpoint {

    private final Expression expression;

    public SpELEndpoint(FieldElement fieldElement, String expression) {
        super(fieldElement);
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
