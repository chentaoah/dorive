package com.gitee.dorive.factory.v1.impl.factory.serializer;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.gitee.dorive.base.v1.executor.api.Context;
import com.gitee.dorive.base.v1.factory.api.EntityTransformer;
import com.gitee.dorive.base.v1.factory.api.FieldAliasMapping;
import com.gitee.dorive.base.v1.factory.api.Serializer;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DefaultSerializer implements Serializer {

    private Class<?> type;
    private EntityTransformer entityTransformer;
    private CopyOptions copyOptions;

    public void initialize() {
        initCopyOptions();
    }

    private void initCopyOptions() {
        this.copyOptions = CopyOptions.create().ignoreNullValue().setFieldNameEditor(field -> {
            FieldAliasMapping fieldAliasMappingByField = entityTransformer.getFieldAliasMappingByField(field);
            return fieldAliasMappingByField != null ? fieldAliasMappingByField.getAlias() : field;

        }).setFieldValueEditor((alias, value) -> {
            FieldAliasMapping fieldAliasMappingByAlias = entityTransformer.getFieldAliasMappingByAlias(alias);
            return fieldAliasMappingByAlias != null ? fieldAliasMappingByAlias.serialize(value) : value;
        });
    }

    @Override
    public Object serialize(Context context, Object object) {
        return BeanUtil.toBean(object, type, copyOptions);
    }

}
