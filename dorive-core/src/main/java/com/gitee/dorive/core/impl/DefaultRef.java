package com.gitee.dorive.core.impl;

import com.gitee.dorive.core.api.EntityHandler;
import com.gitee.dorive.core.api.Ref;
import com.gitee.dorive.core.api.Selector;
import com.gitee.dorive.core.entity.BoundedContext;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

import java.util.Collections;
import java.util.List;

@Data
@AllArgsConstructor
@ToString(exclude = {"all", "self"})
public class DefaultRef implements Ref {

    private EntityHandler entityHandler;
    private List<Object> all;
    private Object self;

    @Override
    public void relay(BoundedContext boundedContext) {
        entityHandler.handleEntities(boundedContext, Collections.singletonList(self));
    }

    @Override
    public void relay(Selector selector) {
        relay(new BoundedContext(selector));
    }

}
