package com.gitee.dorive.core.entity;

import com.gitee.dorive.core.api.Context;
import com.gitee.dorive.core.api.Selector;
import com.gitee.dorive.core.impl.selector.NameSelector;
import lombok.Data;

import java.util.Collections;
import java.util.Map;

@Data
public abstract class AbstractContext implements Context {
    protected Selector selector = NameSelector.EMPTY_SELECTOR;
    protected Map<String, Object> attachments = Collections.emptyMap();
}
