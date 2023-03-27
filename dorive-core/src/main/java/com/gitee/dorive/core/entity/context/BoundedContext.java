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

package com.gitee.dorive.core.entity.context;

import com.gitee.dorive.core.api.context.Selector;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class BoundedContext extends AbstractContext {

    public BoundedContext(Selector selector) {
        setSelector(selector);
    }

    public Object put(String key, Object value) {
        Map<String, Object> attachments = getAttachments();
        if (attachments == Collections.EMPTY_MAP) {
            attachments = new ConcurrentHashMap<>();
            setAttachments(attachments);
        }
        return attachments.put(key, value);
    }

    public boolean containsKey(String key) {
        return getAttachments().containsKey(key);
    }

    public Object get(String key) {
        return getAttachments().get(key);
    }

    public Object remove(String key) {
        return getAttachments().remove(key);
    }

}