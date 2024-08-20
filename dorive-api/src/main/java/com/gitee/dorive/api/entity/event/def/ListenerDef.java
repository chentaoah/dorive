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

import com.gitee.dorive.api.annotation.event.Listener;
import com.gitee.dorive.api.entity.event.ListenerDefinition;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.lang.reflect.AnnotatedElement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ListenerDef {

    private List<String> publishers;
    private Class<?> value;
    private List<String> events;
    private boolean onlyRoot;
    private boolean afterCommit;
    private List<Class<? extends Throwable>> throwExceptions;

    public static ListenerDef fromElement(AnnotatedElement element) {
        Listener listener = AnnotatedElementUtils.getMergedAnnotation(element, Listener.class);
        if (listener != null) {
            ListenerDefinition listenerDefinition = new ListenerDefinition();
            listenerDefinition.setPublisherNames(Arrays.asList(listener.publishers()));
            listenerDefinition.setEntityTypeName(listener.value().getName());
            listenerDefinition.setEventNames(Arrays.asList(listener.events()));
            listenerDefinition.setOnlyRoot(listener.onlyRoot());
            listenerDefinition.setAfterCommit(listener.afterCommit());
            List<String> throwExceptionNames = new ArrayList<>();
            for (Class<? extends Throwable> throwExceptionType : listener.throwExceptions()) {
                throwExceptionNames.add(throwExceptionType.getName());
            }
            listenerDefinition.setThrowExceptionNames(throwExceptionNames);
            listenerDefinition.setGenericTypeName(((Class<?>) element).getName());

            ListenerDef listenerDef = new ListenerDef();
            listenerDef.setPublishers(listenerDefinition.getPublisherNames());
            listenerDef.setValue(listener.value());
            listenerDef.setEvents(listenerDefinition.getEventNames());
            listenerDef.setOnlyRoot(listenerDefinition.isOnlyRoot());
            listenerDef.setAfterCommit(listenerDefinition.isAfterCommit());
            listenerDef.setThrowExceptions(Arrays.asList(listener.throwExceptions()));
            return listenerDef;
        }
        return null;
    }

}
