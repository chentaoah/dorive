package com.gitee.spring.domain.core3.impl;

import com.gitee.spring.domain.core3.api.EntityIndex;

import java.util.List;
import java.util.Map;

public class DefaultEntityIndex implements EntityIndex {

    private Map<Object, Object> idsMap;
    private Map<Object, Object> entityMap;

    public DefaultEntityIndex(List<Map<String, Object>> resultMaps, List<Object> entities) {
//        for (Map<String, Object> resultMap : resultMaps) {
//            resultMap.get("")
//        }
    }

    @Override
    public List<Object> selectList(Object rootEntity) {
        return null;
    }

}
