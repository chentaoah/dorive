package com.gitee.dorive.core.impl.selector;

import com.gitee.dorive.core.api.Selector;

public abstract class AbstractSelector implements Selector {

    @Override
    public Object put(String key, Object value) {
        return null;
    }

    @Override
    public boolean containsKey(String key) {
        return false;
    }

    @Override
    public Object get(String key) {
        return null;
    }

    @Override
    public Object remove(String key) {
        return null;
    }

}
