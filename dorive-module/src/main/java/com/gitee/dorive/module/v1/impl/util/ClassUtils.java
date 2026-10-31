/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.module.v1.impl.util;

import cn.hutool.core.util.StrUtil;

import java.net.URI;
import java.security.CodeSource;
import java.security.ProtectionDomain;

public class ClassUtils {

    public static URI toURI(Class<?> clazz) {
        try {
            ProtectionDomain protectionDomain = clazz.getProtectionDomain();
            CodeSource codeSource = protectionDomain.getCodeSource();
            if (codeSource == null) {
                return null;
            }
            URI codeSourceUri = codeSource.getLocation().toURI();
            if ("jar".equals(codeSourceUri.getScheme())) {
                String newPath = codeSourceUri.getSchemeSpecificPart();
                // file:/x.jar!/BOOT-INF/classes!/
                if (newPath.endsWith("!/BOOT-INF/classes!/")) {
                    newPath = newPath.substring(0, newPath.length() - 20);
                }
                // nested:/x.jar/!BOOT-INF/classes/!/
                if (newPath.endsWith("/!BOOT-INF/classes/!/")) {
                    newPath = newPath.substring(0, newPath.length() - 21);
                    if (newPath.startsWith("nested:")) {
                        newPath = StrUtil.replaceFirst(newPath, "nested:", "file:");
                    }
                }
                if (newPath.endsWith("!/")) {
                    newPath = newPath.substring(0, newPath.length() - 2);
                }
                codeSourceUri = new URI(newPath);
            }
            return codeSourceUri;

        } catch (Exception e) {
            return null;
        }
    }

}
