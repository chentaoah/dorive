package com.gitee.dorive.factory.v1.impl.factory.serializer;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.convert.TypeConverter;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import com.gitee.dorive.base.v1.executor.api.Context;
import com.gitee.dorive.base.v1.factory.api.EntityTransformer;
import com.gitee.dorive.base.v1.factory.api.FieldAliasMapping;
import com.gitee.dorive.factory.v1.api.EntityTransformerManager;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class ValueObjEntitySerializer extends DefaultEntitySerializer {

    private EntityTransformerManager entityTransformerManager;

    // 从hutool源码中拷贝
    protected TypeConverter converter = (type, value) -> {
        if (null == value) {
            return null;
        }
        final String name = value.getClass().getName();
        if (ArrayUtil.contains(new String[]{"cn.hutool.json.JSONObject", "cn.hutool.json.JSONArray"}, name)) {
            return ReflectUtil.invoke(value, "toBean", ObjectUtil.defaultIfNull(type, Object.class));
        }
        return Convert.convertWithCheck(type, value, null, true);
    };

    @Override
    public void initialize() {
        super.initialize();
        if (entityTransformerManager.containMatchedValueObj()) {
            resetCopyOptions();
        }
    }

    private void resetCopyOptions() {
        getCopyOptions().setConverter(((targetType, value) -> {
            if (value == null) {
                return null;
            }
            if (targetType == String.class) {
                // 以下情况，不再使用hutool的类型转换（toString）
                if (value instanceof Collection) {
                    return value;
                }
                if (value instanceof Map) {
                    return value;
                }
                // 注意：值对象的子类实例，不会进入该分支
                if (entityTransformerManager.isValueObjType(value.getClass())) {
                    return value;
                }
            }
            return converter.convert(targetType, value);
        }));
    }

    @Override
    public Object serialize(Context context, Object object) {
        Object pojo = super.serialize(context, object);

        EntityTransformer entityTransformer = getEntityTransformer();
        List<FieldAliasMapping> unmatchedValueObjFields = entityTransformer.getUnmatchedValueObjFields();
        for (FieldAliasMapping fieldAliasMapping : unmatchedValueObjFields) {
            Object valueObj = BeanUtil.getFieldValue(object, fieldAliasMapping.getField());
            valueObj = valueObj != null ? fieldAliasMapping.serialize(valueObj) : null;
            if (valueObj != null) {
                BeanUtil.copyProperties(valueObj, pojo, CopyOptions.create().ignoreNullValue());
            }
        }
        return pojo;
    }
}
