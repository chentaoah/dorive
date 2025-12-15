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

package com.gitee.dorive.launcher.v1.impl.factory;

import com.gitee.dorive.base.v1.common.entity.EntityElement;
import com.gitee.dorive.base.v1.joiner.api.EntityJoiner;
import com.gitee.dorive.base.v1.joiner.api.EntityJoinerFactory;
import com.gitee.dorive.joiner.v1.impl.joiner.DefaultEntityJoiner;

import java.util.List;

public class DefaultEntityJoinerFactory implements EntityJoinerFactory {

    @Override
    @SuppressWarnings("unchecked")
    public EntityJoiner create(String name, Object... args) {
        if ("DefaultEntityJoiner".equals(name)) {
            return new DefaultEntityJoiner((Boolean) args[0], (EntityElement) args[1], (List<Object>) args[2]);
        }
        return null;
    }

}
