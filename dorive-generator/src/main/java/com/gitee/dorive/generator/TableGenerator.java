package com.gitee.dorive.generator;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.gitee.dorive.generator.entity.ClassVO;
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
            List<ClassVO> classVOs = doclet.execute();
            return TableUtils.getTableInfos(tablePrefix, classVOs);
        }
        return null;
    }

    public static void sysout(String tablePrefix, String dirPath) {
        List<TableInfo> tableInfos = execute(tablePrefix, dirPath);
        if (tableInfos != null) {
            List<String> tableNames = tableInfos.stream().map(TableInfo::getTableName).collect(Collectors.toList());
            List<String> tableSqls = tableInfos.stream().map(TableInfo::getTableSql).collect(Collectors.toList());
            System.out.println(StrUtil.join(", ", tableNames) + "\n");
            System.out.println(StrUtil.join("\n\n", tableSqls));
        }
    }

    public static void outputFile(String tablePrefix, String dirPath, String filePath) {
        List<TableInfo> tableInfos = execute(tablePrefix, dirPath);
        if (tableInfos != null) {
            List<String> tableSqls = tableInfos.stream().map(TableInfo::getTableSql).collect(Collectors.toList());
            String content = StrUtil.join("\n\n", tableSqls);
            FileUtil.writeString(content, filePath, StandardCharsets.UTF_8);
        }
    }

}
