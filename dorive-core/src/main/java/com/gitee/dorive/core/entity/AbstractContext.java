package com.gitee.dorive.core.entity;

import com.gitee.dorive.core.api.Context;
import com.gitee.dorive.core.api.Selector;
import com.gitee.dorive.core.impl.selector.NameSelector;
import lombok.Data;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
public abstract class AbstractContext implements Context {

    protected Selector selector = NameSelector.EMPTY_SELECTOR;
    protected Map<String, Object> attachments = Collections.emptyMap();

    @Override
    public Object put(String key, Object value) {
        if (attachments == Collections.EMPTY_MAP) {
            attachments = new ConcurrentHashMap<>();
        }
        return attachments.put(key, value);
    }

    @Override
    public boolean containsKey(String key) {
        return attachments.containsKey(key);
    }

    @Override
    public Object get(String key) {
        return attachments.get(key);
    }

    @Override
    public Object remove(String key) {
        return attachments.remove(key);
    }

}
