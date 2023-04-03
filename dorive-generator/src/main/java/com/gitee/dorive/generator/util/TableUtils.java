/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gitee.dorive.generator.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.gitee.dorive.api.annotation.Entity;
import com.gitee.dorive.generator.entity.ClassVo;
import com.gitee.dorive.generator.entity.FieldVo;
import com.gitee.dorive.generator.entity.TableInfo;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TableUtils {

    public static List<TableInfo> getTableInfos(String tablePrefix, List<ClassVo> classVos) {
        List<TableInfo> tableInfos = new ArrayList<>();
        for (ClassVo classVo : classVos) {
            tableInfos.add(newTableInfo(tablePrefix, classVo));
        }
        return tableInfos;
    }

    public static TableInfo newTableInfo(String tablePrefix, ClassVo classVo) {
        String tableName = tablePrefix + "_" + StrUtil.toUnderlineCase(classVo.getName());
        List<String> properties = new ArrayList<>();
        List<String> alterProperties = new ArrayList<>();
        List<String> tableIndexes = new ArrayList<>();

        List<FieldVo> fieldVos = classVo.getFieldVos();
        String lastColumn = null;
        for (FieldVo fieldVo : fieldVos) {
            if (fieldVo.isAnnotationPresent(Entity.class)) {
                continue;
            }

            String comment = fieldVo.getComment();
            String type = fieldVo.getType();
            String name = fieldVo.getName();
            String column = StrUtil.toUnderlineCase(name);
            
            String property = null;
            switch (name) {
                case "id":
                    if (Integer.class.getTypeName().equals(type)) {
                        properties.add("id int unsigned auto_increment comment '主键' primary key");

                    } else if (Long.class.getTypeName().equals(type)) {
                        properties.add("id bigint unsigned auto_increment comment '主键' primary key");
                    }
                    break;

                case "createUser":
                    property = "create_user varchar(255) null comment '创建者'";
                    break;

                case "createTime":
                    property = "create_time timestamp default CURRENT_TIMESTAMP null comment '创建时间'";
                    break;

                case "updateUser":
                    property = "update_user varchar(255) null comment '更新者'";
                    break;

                case "updateTime":
                    property = "update_time timestamp null on update CURRENT_TIMESTAMP comment '更新时间'";
                    break;

                default:
                    String suffix = StringUtils.isNotBlank(comment) ? " comment '" + comment + "'" : "";
                    if (Integer.class.getTypeName().equals(type)) {
                        property = column + " int null" + suffix;

                    } else if (Long.class.getTypeName().equals(type)) {
                        property = column + " bigint null" + suffix;

                    } else if (String.class.getTypeName().equals(type)) {
                        property = column + " varchar(255) null" + suffix;

                    } else if (Date.class.getTypeName().equals(type)) {
                        property = column + " timestamp null" + suffix;
                    }
                    break;
            }

            if (property != null) {
                properties.add(property);
                alterProperties.add("alter table " + tableName + " add " + property + (lastColumn != null ? " after " + lastColumn : "") + ";");
            }

            lastColumn = column;

            if (!name.equals("id") && name.endsWith("Id")) {
                tableIndexes.add("create index idx_" + column + " on " + tableName + " (" + column + ");");
            }
        }

        String comment = classVo.getComment();
        String suffix = StringUtils.isNotBlank(comment) ? " comment '" + comment + "';" : ";";
        String tableSql = "create table " + tableName + " (\n" +
                CollUtil.join(properties, ",\n", "    ", null)
                + "\n)" + suffix;

        if (!tableIndexes.isEmpty()) {
            tableSql = tableSql + "\n" + StrUtil.join("\n", tableIndexes);
        }

        String alterSql = StrUtil.join("\n", alterProperties);

        return new TableInfo(tableName, tableSql, alterSql);
    }

}
