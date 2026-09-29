/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.base.v1.definition.def;

import com.gitee.dorive.base.v1.definition.annotation.Query;
import lombok.Data;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.lang.reflect.AnnotatedElement;

@Data
public class QueryDef {
    private String method;
    private String countMethod;
    private String[] ignoreFields;
    private String sortByField;
    private String orderField;
    private String pageField;
    private String limitField;

    public static QueryDef fromElement(AnnotatedElement element) {
        Query query = AnnotatedElementUtils.getMergedAnnotation(element, Query.class);
        if (query != null) {
            QueryDef queryDef = new QueryDef();
            queryDef.setMethod(query.method());
            queryDef.setCountMethod(query.countMethod());
            queryDef.setIgnoreFields(query.ignoreFields());
            queryDef.setSortByField(query.sortByField());
            queryDef.setOrderField(query.orderField());
            queryDef.setPageField(query.pageField());
            queryDef.setLimitField(query.limitField());
            return queryDef;
        }
        return null;
    }
}
