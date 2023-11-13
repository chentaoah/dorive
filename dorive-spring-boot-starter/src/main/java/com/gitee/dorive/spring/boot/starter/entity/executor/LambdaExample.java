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

package com.gitee.dorive.spring.boot.starter.entity.executor;

import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.gitee.dorive.core.entity.executor.Example;
import com.gitee.dorive.spring.boot.starter.util.LambdaUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class LambdaExample<T> extends Example {

    public LambdaExample<T> eq(SFunction<T, ?> function, Object value) {
        super.eq(LambdaUtils.toProperty(function), value);
        return this;
    }

    public LambdaExample<T> ne(SFunction<T, ?> function, Object value) {
        super.ne(LambdaUtils.toProperty(function), value);
        return this;
    }

    public LambdaExample<T> gt(SFunction<T, ?> function, Object value) {
        super.gt(LambdaUtils.toProperty(function), value);
        return this;
    }

    public LambdaExample<T> ge(SFunction<T, ?> function, Object value) {
        super.ge(LambdaUtils.toProperty(function), value);
        return this;
    }

    public LambdaExample<T> lt(SFunction<T, ?> function, Object value) {
        super.lt(LambdaUtils.toProperty(function), value);
        return this;
    }

    public LambdaExample<T> le(SFunction<T, ?> function, Object value) {
        super.le(LambdaUtils.toProperty(function), value);
        return this;
    }

    public LambdaExample<T> in(SFunction<T, ?> function, Object value) {
        super.in(LambdaUtils.toProperty(function), value);
        return this;
    }

    public LambdaExample<T> notIn(SFunction<T, ?> function, Object value) {
        super.notIn(LambdaUtils.toProperty(function), value);
        return this;
    }

    public LambdaExample<T> like(SFunction<T, ?> function, Object value) {
        super.like(LambdaUtils.toProperty(function), value);
        return this;
    }

    public LambdaExample<T> notLike(SFunction<T, ?> function, Object value) {
        super.notLike(LambdaUtils.toProperty(function), value);
        return this;
    }

    public LambdaExample<T> isNull(SFunction<T, ?> function) {
        super.isNull(LambdaUtils.toProperty(function));
        return this;
    }

    public LambdaExample<T> isNotNull(SFunction<T, ?> function) {
        super.isNotNull(LambdaUtils.toProperty(function));
        return this;
    }

    @SafeVarargs
    public final LambdaExample<T> orderByAsc(SFunction<T, ?>... functions) {
        if (functions != null && functions.length > 0) {
            List<String> properties = new ArrayList<>(functions.length);
            for (SFunction<T, ?> function : functions) {
                properties.add(LambdaUtils.toProperty(function));
            }
            super.orderByAsc(properties.toArray(new String[0]));
        }
        return this;
    }

    @SafeVarargs
    public final LambdaExample<T> orderByDesc(SFunction<T, ?>... functions) {
        if (functions != null && functions.length > 0) {
            List<String> properties = new ArrayList<>(functions.length);
            for (SFunction<T, ?> function : functions) {
                properties.add(LambdaUtils.toProperty(function));
            }
            super.orderByDesc(properties.toArray(new String[0]));
        }
        return this;
    }

}
