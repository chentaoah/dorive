package com.gitee.dorive.core.entity;

import com.gitee.dorive.core.api.Context;
import com.gitee.dorive.core.api.ExampleBuilder;
import com.gitee.dorive.core.api.Selector;
import com.gitee.dorive.core.impl.selector.NameSelector;
import com.gitee.dorive.core.impl.selector.SceneSelector;
import com.gitee.dorive.core.repository.CommonRepository;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class BoundedContext implements Context {

    private Selector selector = NameSelector.EMPTY_SELECTOR;
    private Map<String, Object> attachments = Collections.emptyMap();

    @Deprecated
    public BoundedContext(String... scenes) {
        this.selector = new SceneSelector(scenes);
    }

    public BoundedContext(Selector selector) {
        this.selector = selector;
    }

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

    @Override
    public boolean matches(CommonRepository repository) {
        return selector.matches(repository);
    }

    @Override
    public List<String> selectColumns(CommonRepository repository) {
        return selector.selectColumns(repository);
    }

    public void putBuilder(String key, ExampleBuilder builder) {
        put(key, builder);
    }

}
