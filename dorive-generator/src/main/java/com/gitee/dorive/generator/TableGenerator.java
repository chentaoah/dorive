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

package com.gitee.dorive.generator;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.gitee.dorive.generator.entity.ClassVo;
import com.gitee.dorive.generator.entity.TableInfo;
import com.gitee.dorive.generator.impl.Doclet;
import com.gitee.dorive.generator.util.TableUtils;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TableGenerator {

    public static List<TableInfo> execute(String tablePrefix, String dirPath) {
        List<String> fileNames = FileUtil.listFileNames(dirPath);
        if (!fileNames.isEmpty()) {
            List<String> sources = new ArrayList<>();
            for (String fileName : fileNames) {
                File file = FileUtil.file(dirPath, fileName);
                sources.add(file.getAbsolutePath());
            }
            Doclet doclet = new Doclet(sources);
            List<ClassVo> classVos = doclet.execute();
            return TableUtils.getTableInfos(tablePrefix, classVos);
        }
        return null;
    }

    public static void sysout(String tablePrefix, String dirPath) {
        List<TableInfo> tableInfos = execute(tablePrefix, dirPath);
        if (tableInfos != null) {
            List<String> tableNames = tableInfos.stream().map(TableInfo::getTableName).collect(Collectors.toList());
            List<String> tableSqls = tableInfos.stream().map(TableInfo::getTableSql).collect(Collectors.toList());
            List<String> alterSqls = tableInfos.stream().map(TableInfo::getAlterSql).collect(Collectors.toList());
            System.out.println(StrUtil.join(", ", tableNames) + "\n");
            System.out.println(StrUtil.join("\n\n", tableSqls));
            System.out.println(StrUtil.join("\n\n", alterSqls));
        }
    }

    public static void outputFile(String tablePrefix, String dirPath, String filePath) {
        List<TableInfo> tableInfos = execute(tablePrefix, dirPath);
        if (tableInfos != null) {
            List<String> tableNames = tableInfos.stream().map(TableInfo::getTableName).collect(Collectors.toList());
            List<String> tableSqls = tableInfos.stream().map(TableInfo::getTableSql).collect(Collectors.toList());
            List<String> alterSqls = tableInfos.stream().map(TableInfo::getAlterSql).collect(Collectors.toList());
            System.out.println(StrUtil.join(", ", tableNames) + "\n");
            FileUtil.writeString(StrUtil.join("\n\n", tableSqls), filePath, StandardCharsets.UTF_8);
            System.out.println(StrUtil.join("\n\n", alterSqls));
        }
    }

}
