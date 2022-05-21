package com.gitee.spring.domain.core.utils;

import cn.hutool.core.util.URLUtil;

import java.util.Set;

public class PathUtils {

    public static String getLastAccessPath(String accessPath) {
        return accessPath.lastIndexOf("/") > 0 ? accessPath.substring(0, accessPath.lastIndexOf("/")) : "/";
    }

    public static String getFieldName(String accessPath) {
        return accessPath.startsWith("/") && accessPath.length() > 1 ? accessPath.substring(accessPath.lastIndexOf("/") + 1) : "";
    }

    public static String getAbsolutePath(String accessPath, String relativePath) {
        accessPath = "https://spring-domain" + accessPath;
        accessPath = URLUtil.completeUrl(accessPath, relativePath);
        return accessPath.replace("https://spring-domain", "");
    }

    public static String getBelongPath(Set<String> allAccessPath, String accessPath) {
        String lastAccessPath = getLastAccessPath(accessPath);
        while (!allAccessPath.contains(lastAccessPath) && !"/".equals(lastAccessPath)) {
            lastAccessPath = PathUtils.getLastAccessPath(lastAccessPath);
        }
        return lastAccessPath;
    }

}
