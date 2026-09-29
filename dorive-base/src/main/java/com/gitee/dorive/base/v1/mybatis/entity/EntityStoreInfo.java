/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.base.v1.mybatis.entity;

import com.gitee.dorive.base.v1.mybatis.api.MethodInvoker;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class EntityStoreInfo {
    private Class<?> mapperClass;
    private Object mapper;
    private Class<?> pojoClass;
    private String tableName;
    private String idProperty;
    private String idColumn;
    private Map<String, String> propAliasMapWithoutPk;
    private Map<String, String> propAliasMap;
    private Map<String, String> aliasPropMap;
    private String selectColumns;
    private Map<String, MethodInvoker> selectMethodMap;
}
