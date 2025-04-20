package com.gitee.dorive.module.impl.util;

import org.springframework.util.PropertyPlaceholderHelper;

public class PlaceholderUtils {

    private static final PropertyPlaceholderHelper PROPERTY_PLACEHOLDER_HELPER =
            new PropertyPlaceholderHelper("${", "}");

    public static boolean contains(String strValue) {
        int startIndex = strValue.indexOf("${");
        if (startIndex != -1) {
            int endIndex = strValue.indexOf("}", startIndex);
            return endIndex != -1 && startIndex < endIndex;
        }
        return false;
    }

    public static String replace(String strValue, PropertyPlaceholderHelper.PlaceholderResolver resolver) {
        return PROPERTY_PLACEHOLDER_HELPER.replacePlaceholders(strValue, resolver);
    }

}
