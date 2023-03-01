package com.gitee.dorive.core.entity;

import com.gitee.dorive.core.api.ExampleBuilder;
import com.gitee.dorive.core.api.Selector;
import com.gitee.dorive.core.impl.selector.SceneSelector;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

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

    public void putBuilder(String key, ExampleBuilder builder) {
        put(key, builder);
    }

}
