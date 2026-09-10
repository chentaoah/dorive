package com.gitee.dorive.factory.v1.impl.factory;

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

    private Class<?> deType;
    private EntityTransformer deEntityTransformer;
    private CopyOptions deCopyOptions;

    public void initialize() {
        initDeCopyOptions();
    }

    private void initDeCopyOptions() {
        this.deCopyOptions = CopyOptions.create().ignoreNullValue().setFieldNameEditor(field -> {
            FieldAliasMapping fieldAliasMappingByField = deEntityTransformer.getFieldAliasMappingByField(field);
            return fieldAliasMappingByField != null ? fieldAliasMappingByField.getAlias() : field;

        }).setFieldValueEditor((alias, value) -> {
            FieldAliasMapping fieldAliasMappingByAlias = deEntityTransformer.getFieldAliasMappingByAlias(alias);
            return fieldAliasMappingByAlias != null ? fieldAliasMappingByAlias.deconstruct(value) : value;
        });
    }

    @Override
    public Object serialize(Context context, Object object) {
        return BeanUtil.toBean(object, deType, deCopyOptions);
    }

}
