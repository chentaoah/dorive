package com.gitee.dorive.joiner.v1.impl.keyGen;

import com.gitee.dorive.base.v1.binder.api.Binder;
import com.gitee.dorive.base.v1.core.api.Context;
import com.gitee.dorive.joiner.v1.api.KeyGenerator;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class MultiEntityKeyGenerator implements KeyGenerator {

    private final List<Binder> binders;

    @Override
    public String generate(Context context, Object entity) {
        StringBuilder keyBuilder = new StringBuilder();
        for (Binder binder : binders) {
            Object boundValue = binder.getBoundValue(context, entity);
            boundValue = binder.input(context, boundValue);
            if (boundValue != null) {
                String key = boundValue.toString();
                keyBuilder.append("(").append(key.length()).append(")").append(key).append(",");
            } else {
                keyBuilder = null;
                break;
            }
        }
        if (keyBuilder != null && keyBuilder.length() > 0) {
            keyBuilder.deleteCharAt(keyBuilder.length() - 1);
            return keyBuilder.toString();
        }
        return null;
    }

}
