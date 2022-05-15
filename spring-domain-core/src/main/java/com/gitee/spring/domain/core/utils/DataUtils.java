package com.gitee.spring.domain.core.utils;

import cn.hutool.core.collection.CollUtil;

import java.util.*;

public class DataUtils {

    @SuppressWarnings("unchecked")
    public static List<Object> intersection(Object value1, Object value2) {
        Collection<Object> collection1 = value1 instanceof Collection ? (Collection<Object>) value1 : Collections.singletonList(value1);
        Collection<Object> collection2 = value2 instanceof Collection ? (Collection<Object>) value2 : Collections.singletonList(value2);
        return (List<Object>) CollUtil.intersection(collection1, collection2);
    }

}
