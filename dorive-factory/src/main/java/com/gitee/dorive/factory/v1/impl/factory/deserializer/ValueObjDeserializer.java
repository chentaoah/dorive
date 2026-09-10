package com.gitee.dorive.factory.v1.impl.factory.deserializer;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.convert.TypeConverter;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import com.gitee.dorive.base.v1.executor.api.Context;
import com.gitee.dorive.base.v1.factory.api.EntityTransformer;
import com.gitee.dorive.base.v1.factory.api.FieldAliasMapping;
import com.gitee.dorive.factory.v1.api.EntityTransformerManager;
import com.gitee.dorive.factory.v1.util.TypeUtils;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class ValueObjDeserializer extends DefaultDeserializer {

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
            if (value instanceof String) {
                // 以下情况，不再使用hutool的类型转换（toString）
                Class<?> rawType = TypeUtils.getRawType(targetType);
                if (rawType == null) {
                    throw new RuntimeException("The rawType is null!");
                }
                if (Collection.class.isAssignableFrom(rawType)) {
                    return value;
                }
                if (Map.class.isAssignableFrom(rawType)) {
                    return value;
                }
                if (entityTransformerManager.isValueObjType(rawType)) {
                    return value;
                }
            }
            return converter.convert(targetType, value);
        }));
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object deserialize(Context context, Object object) {
        Object entity = super.deserialize(context, object);

        EntityTransformer entityTransformer = getEntityTransformer();
        Map<String, Object> resultMap = (Map<String, Object>) object;
        List<FieldAliasMapping> unmatchedValueObjFields = entityTransformer.getUnmatchedValueObjFields();
        for (FieldAliasMapping fieldAliasMapping : unmatchedValueObjFields) {
            Object valueObj = fieldAliasMapping.deserialize(resultMap);
            if (valueObj != null) {
                BeanUtil.setFieldValue(entity, fieldAliasMapping.getField(), valueObj);
            }
        }
        return entity;
    }
}
