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

package com.gitee.dorive.module.entity;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.StrUtil;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

@Data
public class ModuleDefinition {
    private String originId;
    private String project;
    private String domain;
    private String subdomain;
    private String version;
    private String module;
    private String moduleType;
    private List<String> configs;
    private List<String> exports;
    private List<String> externalDepends;
    private String tablePrefix;

    public ModuleDefinition(Manifest manifest) {
        Assert.notNull(manifest, "The manifest can not be null!");
        Attributes mainAttributes = manifest.getMainAttributes();

        String originId = mainAttributes.getValue("Dorive-Origin-Id");
        String project = mainAttributes.getValue("Dorive-Project");
        String domain = mainAttributes.getValue("Dorive-Domain");
        String subdomain = mainAttributes.getValue("Dorive-Subdomain");
        String version = mainAttributes.getValue("Dorive-Version");

        String module = mainAttributes.getValue("Dorive-Module");
        String moduleType = mainAttributes.getValue("Dorive-Module-Type");

        String configs = mainAttributes.getValue("Dorive-Configs");
        String exports = mainAttributes.getValue("Dorive-Exports");
        String externalDepends = mainAttributes.getValue("Dorive-External-Depends");
        String tablePrefix = mainAttributes.getValue("Dorive-Table-Prefix");

        this.originId = filterValue(originId);
        this.project = filterValue(project);
        this.domain = filterValue(domain);
        this.subdomain = filterValue(subdomain);
        this.version = filterValue(version);

        this.module = filterValue(module);
        this.moduleType = filterValue(moduleType);

        this.configs = filterValues(configs);
        this.exports = filterValues(exports);
        this.externalDepends = filterValues(externalDepends);
        this.tablePrefix = filterValue(tablePrefix);
    }

    private String filterValue(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        if ("null".equals(value)) {
            return null;
        }
        return value;
    }

    private List<String> filterValues(String values) {
        if (StringUtils.isBlank(values)) {
            return Collections.emptyList();
        }
        if ("null".equals(values)) {
            return Collections.emptyList();
        }
        return StrUtil.splitTrim(values, ",");
    }

    public String getBasePackage() {
        List<String> packages = new ArrayList<>(4);
        if (StringUtils.isNotBlank(project)) {
            packages.add(project);
        }
        if (StringUtils.isNotBlank(domain)) {
            packages.add(domain);
        }
        if (StringUtils.isNotBlank(subdomain)) {
            packages.add(subdomain);
        }
        if (StringUtils.isNotBlank(version)) {
            packages.add(version);
        }
        return StrUtil.join(".", packages);
    }

    public String getMainClassName() {
        return getBasePackage() + ".Application";
    }

    public Class<?> getMainClass() {
        try {
            return ClassUtil.loadClass(getMainClassName());
        } catch (Exception e) {
            return null;
        }
    }

    public List<String> getProfiles() {
        if (configs != null && !configs.isEmpty()) {
            List<String> profiles = new ArrayList<>(configs.size());
            for (String config : configs) {
                if (config.startsWith("application-") && config.endsWith(".yml")) {
                    String profile = config.substring(12, config.length() - 4);
                    profiles.add(profile);
                }
            }
            return profiles;
        }
        return Collections.emptyList();
    }

    public int getOrder() {
        if ("base".equals(moduleType)) {
            return 1;
        } else if ("biz".equals(moduleType)) {
            return 2;
        } else if ("launcher".equals(moduleType)) {
            return 3;
        }
        return 2;
    }
}
