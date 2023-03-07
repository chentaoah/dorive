package com.gitee.dorive.core.entity;

import com.gitee.dorive.core.api.Selector;
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
