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

package com.gitee.dorive.mybatis_plus.impl;

import com.gitee.dorive.core.api.common.ImplFactory;
import com.gitee.dorive.core.api.format.SqlFormat;
import com.gitee.dorive.mybatis.api.sql.SqlRunner;

public class DefaultImplFactory implements ImplFactory {

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getInstance(Class<T> clazz, Object... args) {
        if (clazz == SqlFormat.class) {
            return (T) new DefaultSqlHelper();

        } else if (clazz == SqlRunner.class) {
            return (T) new DefaultSqlHelper();
        }
        return null;
    }

}
