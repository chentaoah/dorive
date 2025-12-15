package com.gitee.dorive.joiner.v1.impl.keyGen;

import com.gitee.dorive.base.v1.binder.api.Binder;
import com.gitee.dorive.base.v1.core.api.Context;
import com.gitee.dorive.joiner.v1.api.KeyGenerator;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SingleFieldKeyGenerator implements KeyGenerator {

    private final Binder binder;

    @Override
    public String generate(Context context, Object entity) {
        Object fieldValue = binder.getFieldValue(context, entity);
        return fieldValue != null ? fieldValue.toString() : null;
    }

}
