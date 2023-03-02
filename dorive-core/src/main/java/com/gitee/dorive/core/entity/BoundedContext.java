package com.gitee.dorive.core.entity;

import com.gitee.dorive.core.api.ExampleBuilder;
import com.gitee.dorive.core.api.Selector;
import com.gitee.dorive.core.impl.selector.SceneSelector;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class BoundedContext extends AbstractContext {

    @Deprecated
    public BoundedContext(String... scenes) {
        this.selector = new SceneSelector(scenes);
    }

    public BoundedContext(Selector selector) {
        this.selector = selector;
    }

    public Object put(String key, Object value) {
        if (attachments == Collections.EMPTY_MAP) {
            attachments = new ConcurrentHashMap<>();
        }
        return attachments.put(key, value);
    }

    public boolean containsKey(String key) {
        return attachments.containsKey(key);
    }

    public Object get(String key) {
        return attachments.get(key);
    }

    public Object remove(String key) {
        return attachments.remove(key);
    }

    public void putBuilder(String key, ExampleBuilder builder) {
        put(key, builder);
    }

}
