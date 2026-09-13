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

package com.gitee.dorive.executor.v1.impl.builder;

import cn.hutool.core.util.ReflectUtil;
import com.baomidou.mybatisplus.core.toolkit.LambdaUtils;
import com.baomidou.mybatisplus.core.toolkit.support.LambdaMeta;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.gitee.dorive.base.v1.executor.api.Options;
import com.gitee.dorive.base.v1.executor.api.Selector;
import com.gitee.dorive.base.v1.executor.entity.ctx.DefaultOptions;
import com.gitee.dorive.executor.v1.api.IndexProvider;
import com.gitee.dorive.executor.v1.impl.index.LambdaIndexProvider;
import com.gitee.dorive.executor.v1.impl.index.NameIndexProvider;
import com.gitee.dorive.executor.v1.impl.index.TypeIndexProvider;
import com.gitee.dorive.executor.v1.entity.Selection;
import com.gitee.dorive.executor.v1.impl.selector.DefaultSelector;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.reflection.property.PropertyNamer;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class Builder {
    private String[] names;
    private Class<?>[] types;
    private List<Field> fields;
    private String[] selections;

    public static Options build(String... names) {
        return new Builder().match(names).build();
    }

    public static Options build(Class<?>... types) {
        return new Builder().match(types).build();
    }

    public Builder match(String... names) {
        this.names = names;
        return this;
    }

    public Builder match(Class<?>... types) {
        this.types = types;
        return this;
    }

    public <T> Builder match(SFunction<T, ?> function) {
        LambdaMeta meta = LambdaUtils.extract(function);
        Class<?> instantiatedClass = meta.getInstantiatedClass();
        String fieldName = PropertyNamer.methodToProperty(meta.getImplMethodName());
        java.lang.reflect.Field field = ReflectUtil.getField(instantiatedClass, fieldName);
        if (fields == null) {
            this.fields = new ArrayList<>(4);
        }
        fields.add(field);
        return this;
    }

    public Builder select(String... selections) {
        this.selections = selections;
        return this;
    }

    public Options build() {
        // IndexProvider
        IndexProvider indexProvider = null;
        List<Selection> selections = null;
        if (names != null && names.length > 0) {
            NameIndexProvider nameIndexProvider = new NameIndexProvider(names);
            indexProvider = nameIndexProvider;
            selections = nameIndexProvider.getSelections();

        } else if (types != null && types.length > 0) {
            if (types.length == 1 && (fields != null && !fields.isEmpty())) {
                indexProvider = new LambdaIndexProvider(types[0], fields);
            } else {
                indexProvider = new TypeIndexProvider(types);
            }
        }

        // Selector
        DefaultSelector defaultSelector = new DefaultSelector(indexProvider, selections);
        if (this.selections != null && this.selections.length > 0) {
            defaultSelector.setSelections(Arrays.stream(this.selections).map(this::newSelection).collect(Collectors.toList()));
        }

        // Options
        Options options = new DefaultOptions();
        options.setOption(Selector.class, defaultSelector);
        return options;
    }

    private Selection newSelection(String string) {
        return StringUtils.isNotBlank(string) ? new Selection(string) : null;
    }
}
