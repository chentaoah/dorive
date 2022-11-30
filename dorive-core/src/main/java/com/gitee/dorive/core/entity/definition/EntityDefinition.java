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
package com.gitee.dorive.core.entity.definition;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.gitee.dorive.core.entity.executor.OrderBy;
import com.gitee.dorive.core.annotation.Entity;
import com.gitee.dorive.core.api.constant.Order;
import com.gitee.dorive.core.entity.EntityElement;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.AnnotationUtils;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EntityDefinition {

    private String[] scenes;
    private int order;
    private Class<?> mapper;
    private Class<?> factory;
    private Class<?> repository;
    private String orderByAsc;
    private String orderByDesc;
    private String builderKey;
    private String commandKey;

    public static EntityDefinition newEntityDefinition(EntityElement entityElement) {
        Entity entityAnnotation = entityElement.getEntityAnnotation();
        Map<String, Object> annotationAttributes = AnnotationUtils.getAnnotationAttributes(entityAnnotation);
        return BeanUtil.copyProperties(annotationAttributes, EntityDefinition.class);
    }

    public OrderBy getDefaultOrderBy() {
        if (StringUtils.isNotBlank(this.orderByAsc)) {
            String orderByAsc = StrUtil.toUnderlineCase(this.orderByAsc);
            String[] columns = StrUtil.splitTrim(orderByAsc, ",").toArray(new String[0]);
            return new OrderBy(columns, Order.ASC);
        }
        if (StringUtils.isNotBlank(this.orderByDesc)) {
            String orderByDesc = StrUtil.toUnderlineCase(this.orderByDesc);
            String[] columns = StrUtil.splitTrim(orderByDesc, ",").toArray(new String[0]);
            return new OrderBy(columns, Order.DESC);
        }
        return null;
    }

}
