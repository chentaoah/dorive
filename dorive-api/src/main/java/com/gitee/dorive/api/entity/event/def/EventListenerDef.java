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

package com.gitee.dorive.api.entity.event.def;

import com.gitee.dorive.api.annotation.event.EventListener;
import com.gitee.dorive.api.constant.enums.OperationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.lang.reflect.AnnotatedElement;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventListenerDef {

    private Class<?> value;
    private OperationType[] subscribeTo;
    private boolean afterCommit;
    private Class<? extends Throwable>[] rollbackFor;

    public static EventListenerDef fromElement(AnnotatedElement element) {
        EventListener eventListener = AnnotatedElementUtils.getMergedAnnotation(element, EventListener.class);
        if (eventListener != null) {
            EventListenerDef eventListenerDef = new EventListenerDef();
            eventListenerDef.setValue(eventListener.value());
            eventListenerDef.setSubscribeTo(eventListener.subscribeTo());
            eventListenerDef.setAfterCommit(eventListener.afterCommit());
            eventListenerDef.setRollbackFor(eventListener.rollbackFor());
            return eventListenerDef;
        }
        return null;
    }

}
