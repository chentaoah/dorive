package com.gitee.dorive.factory.v1.impl.factory.serializer;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.gitee.dorive.base.v1.executor.api.Context;
import com.gitee.dorive.base.v1.factory.api.EntityTransformer;
import com.gitee.dorive.base.v1.factory.api.entity.EntitySerializer;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DefaultEntitySerializer implements EntitySerializer {

    private Class<?> type;
    private EntityTransformer entityTransformer;
    private CopyOptions copyOptions;

    public void initialize() {
        initCopyOptions();
    }

    private void initCopyOptions() {
        this.copyOptions = CopyOptions.create() //
                .ignoreNullValue() //
                .setFieldNameEditor(field -> entityTransformer.serialize(field)) //
                .setFieldValueEditor((alias, value) -> {
                    String field = entityTransformer.deserialize(alias);
                    return entityTransformer.serialize(field, value);
                });
    }

    @Override
    public Object serialize(Context context, Object object) {
        return BeanUtil.toBean(object, type, copyOptions);
    }

}
