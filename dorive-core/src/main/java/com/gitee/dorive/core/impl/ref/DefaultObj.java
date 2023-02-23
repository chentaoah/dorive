package com.gitee.dorive.core.impl.ref;

import com.gitee.dorive.core.api.EntityHandler;
import com.gitee.dorive.core.api.Obj;
import com.gitee.dorive.core.api.Selector;
import com.gitee.dorive.core.entity.BoundedContext;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Collections;

@Data
@AllArgsConstructor
public class DefaultObj implements Obj {

    private DefaultRef ref;
    private Object object;

    @Override
    public int select(BoundedContext boundedContext) {
        EntityHandler entityHandler = ref.getEntityHandler();
        return entityHandler.handleEntities(boundedContext, Collections.singletonList(object));
    }

    @Override
    public int select(Selector selector) {
        return select(new BoundedContext(selector));
    }

}
