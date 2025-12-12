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

package com.gitee.dorive.module.v1.impl.util;

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
                if (newPath.endsWith("!/BOOT-INF/classes!/")) {
                    newPath = newPath.substring(0, newPath.length() - 20);
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
