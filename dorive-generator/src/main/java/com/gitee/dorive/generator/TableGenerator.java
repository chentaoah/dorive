package com.gitee.dorive.generator;

import cn.hutool.core.io.FileUtil;
import com.gitee.dorive.generator.entity.ClassVO;
import com.gitee.dorive.generator.entity.TableInfo;
import com.gitee.dorive.generator.impl.Doclet;
import com.gitee.dorive.generator.util.TableUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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

}
