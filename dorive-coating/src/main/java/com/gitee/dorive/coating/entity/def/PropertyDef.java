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

package com.gitee.dorive.coating.entity.def;

import cn.hutool.core.bean.BeanUtil;
import com.gitee.dorive.coating.annotation.Property;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.lang.reflect.Field;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PropertyDef {

    private String belongTo;
    private String field;
    private String operator;
    private boolean ignore;

    public static PropertyDef fromElement(Field field) {
        if (field.isAnnotationPresent(Property.class)) {
            Map<String, Object> attributes = AnnotatedElementUtils.getMergedAnnotationAttributes(field, Property.class);
            PropertyDef propertyDef = BeanUtil.copyProperties(attributes, PropertyDef.class);
            if (StringUtils.isBlank(propertyDef.getField())) {
                propertyDef.setField(field.getName());
            }
            return propertyDef;
        }
        return new PropertyDef("/", field.getName(), "=", false);
    }

}
