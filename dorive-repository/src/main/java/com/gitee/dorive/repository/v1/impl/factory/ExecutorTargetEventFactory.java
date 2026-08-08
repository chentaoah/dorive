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

package com.gitee.dorive.repository.v1.impl.factory;

import cn.hutool.core.bean.BeanUtil;
import com.gitee.dorive.base.v1.core.api.Context;
import com.gitee.dorive.base.v1.core.entity.op.EntityOp;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

import java.util.List;

@Getter
@Setter
public class ExecutorTargetEventFactory extends ExecutorEventFactory {

    private final Class<?> target;

    public ExecutorTargetEventFactory(Class<?> source, Class<?> target) {
        super(source);
        this.target = target;
    }

    @Override
    public ApplicationEvent newApplicationEvent(Object source, boolean root, Class<?> entityClass, Context context, EntityOp entityOp) {
        ApplicationEvent applicationEvent = super.newApplicationEvent(source, root, entityClass, context, entityOp);
        if (applicationEvent != null) {
            List<?> entities = entityOp.getEntities();
            if (entities.size() == 1) {
                return (ApplicationEvent) BeanUtil.copyProperties(entities.get(0), target);
            }
        }
        return null;
    }

}
