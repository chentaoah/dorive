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

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.StrUtil;
import com.gitee.dorive.module.impl.util.NameUtils;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.Resource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import static com.gitee.dorive.module.impl.parser.AbstractModuleParser.PATH_MATCHER;

@Data
public class ModuleDefinition {
    private Resource resource;
    private String originId;
    private String organization;
    private String project;
    private String domain;
    private String subdomain;
    private String name;
    private String version;
    private String type;
    private List<String> tags;
    private List<String> profiles;
    private List<String> configs;
    private List<String> exports;
    private List<String> requires;
    private List<String> provides;
    private List<String> notifies;
    private List<String> waits;
    private String tablePrefix;
    private String requestPrefix;

    public ModuleDefinition(Resource resource, Manifest manifest) {
        Assert.notNull(resource, "The resource can not be null!");
        Assert.notNull(manifest, "The manifest can not be null!");

        this.resource = resource;
        Attributes mainAttributes = manifest.getMainAttributes();

        String originId = mainAttributes.getValue("Dorive-Origin-Id");
        String organization = mainAttributes.getValue("Dorive-Organization");
        String project = mainAttributes.getValue("Dorive-Project");
        String domain = mainAttributes.getValue("Dorive-Domain");
        String subdomain = mainAttributes.getValue("Dorive-Subdomain");

        String name = mainAttributes.getValue("Dorive-Module");
        String version = mainAttributes.getValue("Dorive-Version");
        String type = mainAttributes.getValue("Dorive-Type");
        String tags = mainAttributes.getValue("Dorive-Tags");

        String profiles = mainAttributes.getValue("Dorive-Profiles");
        String configs = mainAttributes.getValue("Dorive-Configs");
        String exports = mainAttributes.getValue("Dorive-Exports");
        String requires = mainAttributes.getValue("Dorive-Requires");
        String provides = mainAttributes.getValue("Dorive-Provides");
        String notifies = mainAttributes.getValue("Dorive-Notifies");
        String waits = mainAttributes.getValue("Dorive-Waits");
        String tablePrefix = mainAttributes.getValue("Dorive-Table-Prefix");
        String requestPrefix = mainAttributes.getValue("Dorive-Request-Prefix");

        this.originId = filterValue(originId);
        this.organization = filterValue(organization);
        this.project = filterValue(project);
        this.domain = filterValue(domain);
        this.subdomain = filterValue(subdomain);

        this.name = filterValue(name);
        this.version = filterValue(version);
        this.type = filterValue(type);
        this.tags = filterValues(tags);

        this.profiles = filterValues(profiles);
        this.configs = filterValues(configs);
        this.exports = filterValues(exports);
        this.requires = filterValues(requires);
        this.provides = filterValues(provides);
        this.notifies = filterValues(notifies);
        this.waits = filterValues(waits);
        this.tablePrefix = filterValue(tablePrefix);
        this.requestPrefix = filterValue(requestPrefix);
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

    public String getScanPackage() {
        List<String> packages = new ArrayList<>(2);
        if (StringUtils.isNotBlank(organization)) {
            packages.add(NameUtils.toPackage(organization));
        }
        if (StringUtils.isNotBlank(project)) {
            packages.add(NameUtils.toPackage(project));
        }
        return StrUtil.join(".", packages) + ".**";
    }

    public boolean isExposed(Class<?> clazz) {
        String className = clazz.getName();
        return CollUtil.findOne(exports, export -> PATH_MATCHER.match(export, className)) != null;
    }

    public String getDomainPath() {
        List<String> packages = new ArrayList<>(2);
        if (StringUtils.isNotBlank(project)) {
            packages.add(project);
        }
        if (StringUtils.isNotBlank(domain)) {
            packages.add(domain);
        }
        return StrUtil.join(".", packages);
    }

    public String getBasePackage() {
        List<String> packages = new ArrayList<>(5);
        if (StringUtils.isNotBlank(organization)) {
            packages.add(NameUtils.toPackage(organization));
        }
        if (StringUtils.isNotBlank(project)) {
            packages.add(NameUtils.toPackage(project));
        }
        if (StringUtils.isNotBlank(domain)) {
            packages.add(NameUtils.toPackage(domain));
        }
        if (StringUtils.isNotBlank(subdomain)) {
            packages.add(NameUtils.toPackage(subdomain));
        }
        if (StringUtils.isNotBlank(version)) {
            packages.add(NameUtils.toPackage(version));
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
        List<String> activeProfiles = Collections.emptyList();
        if (profiles != null && !profiles.isEmpty()) {
            activeProfiles = new ArrayList<>(profiles);
        }
        if (configs != null && !configs.isEmpty()) {
            if (activeProfiles.isEmpty()) {
                activeProfiles = new ArrayList<>(configs.size());
            }
            for (String config : configs) {
                if (config.startsWith("application-") && config.endsWith(".yml")) {
                    String profile = config.substring(12, config.length() - 4);
                    activeProfiles.add(profile);
                }
            }
        }
        return activeProfiles;
    }

    public int getOrder() {
        if ("base".equals(type)) {
            return 1;
        } else if ("biz".equals(type)) {
            return 2;
        } else if ("launcher".equals(type)) {
            return 3;
        }
        return 2;
    }

    public String getPropertiesPrefix() {
        return name + "." + version + ".";
    }

    public String getModulePathKey() {
        return name + "." + version + ".module_path";
    }

    public String getModulePathValue() {
        return StringUtils.isNotBlank(requestPrefix) ? requestPrefix : name + "/" + version;
    }

    @Override
    public String toString() {
        List<String> paths = new ArrayList<>(5);
        paths.add(String.valueOf(project));
        paths.add(String.valueOf(domain));
        paths.add(String.valueOf(subdomain));
        paths.add(String.valueOf(name));
        paths.add(String.valueOf(version));
        return StrUtil.join(".", paths);
    }
}
