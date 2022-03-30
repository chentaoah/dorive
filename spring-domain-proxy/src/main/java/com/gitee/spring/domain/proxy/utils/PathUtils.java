package com.gitee.spring.domain.proxy.utils;

public class PathUtils {

    public static String getLastAccessPath(String accessPath) {
        return accessPath.lastIndexOf("/") > 0 ? accessPath.substring(0, accessPath.lastIndexOf("/")) : "/";
    }

    public static String getFieldName(String accessPath) {
        return accessPath.startsWith("/") && accessPath.length() > 1 ? accessPath.substring(accessPath.lastIndexOf("/") + 1) : "";
    }

}
