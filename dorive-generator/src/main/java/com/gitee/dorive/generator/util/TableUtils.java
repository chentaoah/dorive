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
import com.gitee.dorive.generator.entity.ClassVO;
import com.gitee.dorive.generator.entity.FieldVO;
import com.gitee.dorive.generator.entity.TableInfo;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TableUtils {

    public static List<TableInfo> getTableInfos(String tablePrefix, List<ClassVO> classVOs) {
        List<TableInfo> tableInfos = new ArrayList<>();
        for (ClassVO classVO : classVOs) {
            tableInfos.add(newTableInfo(tablePrefix, classVO));
        }
        return tableInfos;
    }

    public static TableInfo newTableInfo(String tablePrefix, ClassVO classVO) {
        String tableName = tablePrefix + "_" + StrUtil.toUnderlineCase(classVO.getName());
        List<String> properties = new ArrayList<>();
        List<String> tableIndexes = new ArrayList<>();

        List<FieldVO> fieldVOs = classVO.getFieldVOs();
        for (FieldVO fieldVO : fieldVOs) {
            if (fieldVO.isAnnotationPresent(Entity.class)) {
                continue;
            }

            String fieldName = fieldVO.getName();
            String column = StrUtil.toUnderlineCase(fieldName);

            if (!fieldName.equals("id") && fieldName.endsWith("Id")) {
                tableIndexes.add("create index idx_" + column + " on " + tableName + " (" + column + ");");
            }

            switch (fieldName) {
                case "id":
                    properties.add("id int unsigned auto_increment comment '主键' primary key");
                    break;

                case "createUser":
                    properties.add("create_user varchar(255) null comment '创建者'");
                    break;

                case "createTime":
                    properties.add("create_time timestamp default CURRENT_TIMESTAMP null comment '创建时间'");
                    break;

                case "updateUser":
                    properties.add("update_user varchar(255) null comment '更新者'");
                    break;

                case "updateTime":
                    properties.add("update_time timestamp null on update CURRENT_TIMESTAMP comment '更新时间'");
                    break;

                default:
                    String comment = fieldVO.getComment();
                    String type = fieldVO.getType();
                    String suffix = StringUtils.isNotBlank(comment) ? " comment '" + comment + "'" : "";

                    if (Integer.class.getTypeName().equals(type)) {
                        properties.add(column + " int null" + suffix);

                    } else if (String.class.getTypeName().equals(type)) {
                        properties.add(column + " varchar(255) null" + suffix);

                    } else if (Date.class.getTypeName().equals(type)) {
                        properties.add(column + " timestamp null" + suffix);
                    }
                    break;
            }
        }

        String comment = classVO.getComment();
        String suffix = StringUtils.isNotBlank(comment) ? " comment '" + comment + "';" : ";";
        String tableSql = "create table " + tableName + " (\n" +
                CollUtil.join(properties, ",\n", "    ", null)
                + "\n)" + suffix;

        if (!tableIndexes.isEmpty()) {
            tableSql = tableSql + "\n" + StrUtil.join("\n", tableIndexes);
        }

        return new TableInfo(tableName, tableSql);
    }

}
