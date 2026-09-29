/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.base.v1.executor.entity.ctx;

import com.gitee.dorive.base.v1.executor.api.Context;
import com.gitee.dorive.base.v1.executor.api.Options;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DefaultContext extends DefaultOptions implements Context {

    private Map<String, Object> attachments = new ConcurrentHashMap<>(8);

    public DefaultContext(Options options) {
        super(options);
    }

    public DefaultContext(Context context) {
        super(context);
        this.attachments.putAll(context.getAttachments());
    }

    public DefaultContext(Options options, Context context) {
        super(options);
        this.attachments.putAll(context.getAttachments());
    }

    @Override
    public void setAttachment(String name, Object value) {
        attachments.put(name, value);
    }

    @Override
    public Object getAttachment(String name) {
        return attachments.get(name);
    }

    @Override
    public void removeAttachment(String name) {
        attachments.remove(name);
    }

}
