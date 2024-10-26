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

package com.gitee.dorive.event.impl.listener.adapter;

import cn.hutool.core.util.ArrayUtil;
import com.gitee.dorive.api.entity.event.def.ListenerDef;
import com.gitee.dorive.event.api.EntityEventListener;
import com.gitee.dorive.event.entity.EntityEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
@NoArgsConstructor
@AllArgsConstructor
public class RouteListenerAdapter implements EntityEventListener {

    private ListenerDef listenerDef;
    private EntityEventListener entityEventListener;

    @Override
    public void onEntityEvent(EntityEvent entityEvent) {
        boolean matchPublisher = ArrayUtil.contains(listenerDef.getPublishers(), entityEvent.getPublisher());
        boolean matchEvent = ArrayUtil.contains(listenerDef.getEvents(), entityEvent.getName());
        boolean matchOnlyRoot = !listenerDef.isOnlyRoot() || entityEvent.isRoot();

        if (matchPublisher && matchEvent && matchOnlyRoot) {
            entityEventListener.onEntityEvent(entityEvent);
        }
    }

}
