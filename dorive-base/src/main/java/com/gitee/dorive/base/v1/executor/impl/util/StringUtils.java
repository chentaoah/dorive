/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.base.v1.executor.impl.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class StringUtils {

    public static List<String> toList(Object object) {
        if (object instanceof String) {
            List<String> list = new ArrayList<>(1);
            list.add((String) object);
            return list;

        } else if (object instanceof String[]) {
            return new ArrayList<>(Arrays.asList((String[]) object));

        } else if (object instanceof Collection<?> collection) {
            List<String> list = new ArrayList<>(collection.size());
            for (Object item : collection) {
                list.add(item.toString());
            }
            return list;
        }
        return null;
    }

}
