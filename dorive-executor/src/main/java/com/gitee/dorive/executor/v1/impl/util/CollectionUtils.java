/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.executor.v1.impl.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class CollectionUtils {

    public static List<?> toList(Object object) {
        if (object instanceof List) {
            return (List<?>) object;

        } else if (object instanceof Collection) {
            return new ArrayList<>((Collection<?>) object);

        } else {
            return Collections.singletonList(object);
        }
    }

}
