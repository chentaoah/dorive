package com.gitee.spring.boot.starter.dorive.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.gitee.dorive.coating.util.ResourceUtils;
import com.gitee.dorive.core.annotation.Entity;
import lombok.AllArgsConstructor;

import javax.xml.crypto.Data;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class TableUtils {

    public static List<TableInfo> getTableInfos(String tablePrefix, String scanPackage) throws Exception {
        List<Class<?>> entityClasses = ResourceUtils.resolveClasses(scanPackage);
        List<TableInfo> tableInfos = new ArrayList<>();
        for (Class<?> entityClass : entityClasses) {
            tableInfos.add(newTableInfo(tablePrefix, entityClass));
        }
        return tableInfos;
    }

    public static TableInfo newTableInfo(String tablePrefix, Class<?> entityClass) {
        String tableName = tablePrefix + "_" + StrUtil.toUnderlineCase(entityClass.getSimpleName());

        Field[] fields = ReflectUtil.getFields(entityClass);
        List<String> properties = new ArrayList<>();

        for (Field field : fields) {
            if (field.isAnnotationPresent(Entity.class)) {
                continue;
            }

            String name = field.getName();
            String column = StrUtil.toUnderlineCase(name);

            if (name.equals("id")) {
                properties.add("id int auto_increment primary key");

            } else if (name.equals("createTime")) {
                properties.add("create_time timestamp default CURRENT_TIMESTAMP null");

            } else {
                Class<?> type = field.getType();
                if (type == Integer.class) {
                    properties.add(column + " int null");

                } else if (type == String.class) {
                    properties.add(column + " varchar(255) null");

                } else if (type == Data.class) {
                    properties.add(column + " timestamp null");
                }
            }
        }

        String tableCreation = "create table " + tableName + "(\n" +
                CollUtil.join(properties, ",\n", "    ", null)
                + "\n);";

        return new TableInfo(tableName, tableCreation);
    }

    @lombok.Data
    @AllArgsConstructor
    public static class TableInfo {
        private String tableName;
        private String tableCreation;
    }

}
