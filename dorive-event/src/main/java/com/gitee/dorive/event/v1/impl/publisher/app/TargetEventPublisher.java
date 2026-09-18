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

package com.gitee.dorive.event.v1.impl.publisher.app;

import cn.hutool.core.bean.BeanUtil;
import com.gitee.dorive.base.v1.executor.entity.eop.EntityOp;
import com.gitee.dorive.event.v1.entity.BaseEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.lang.NonNull;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class TargetEventPublisher implements ApplicationEventPublisher {

    private final Class<?> target;
    private final ApplicationEventPublisher publisher;

    @Override
    public void publishEvent(@NonNull Object event) {
        BaseEvent<?> baseEvent = (BaseEvent<?>) event;
        EntityOp entityOp = baseEvent.getEntityOp();
        List<?> entities = entityOp.getEntities();
        if (entities.size() == 1) {
            Object newEvent = BeanUtil.copyProperties(entities.get(0), target);
            if (newEvent != null) {
                publisher.publishEvent(newEvent);
            }
        }
    }

}
