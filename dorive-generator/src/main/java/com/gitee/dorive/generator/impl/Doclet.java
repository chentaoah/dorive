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

package com.gitee.dorive.generator.impl;

import cn.hutool.core.util.ReflectUtil;
import com.gitee.dorive.generator.entity.AnnotationVO;
import com.gitee.dorive.generator.entity.ClassVO;
import com.gitee.dorive.generator.entity.FieldVO;
import com.sun.javadoc.AnnotationDesc;
import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.FieldDoc;
import com.sun.javadoc.RootDoc;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Doclet {

    private static RootDoc rootDoc;
    private final List<String> sources;

    public static boolean start(RootDoc rootDoc) {
        Doclet.rootDoc = rootDoc;
        return true;
    }

    public Doclet(List<String> sources) {
        this.sources = sources;
    }

    public List<ClassVO> execute() {
        List<String> arguments = new ArrayList<>();
        arguments.add("-doclet");
        arguments.add(Doclet.class.getName());
        arguments.add("-docletpath");
        arguments.add(Objects.requireNonNull(Doclet.class.getResource("/")).getPath());
        arguments.add("-encoding");
        arguments.add("utf-8");
        arguments.addAll(sources);
        com.sun.tools.javadoc.Main.execute(arguments.toArray(new String[0]));

        ClassDoc[] classDocs = rootDoc.classes();
        if (classDocs == null || classDocs.length == 0) {
            throw new RuntimeException("No classes information found!");
        }

        List<ClassVO> classVOs = new ArrayList<>();

        for (ClassDoc classDoc : classDocs) {
            ClassVO classVO = new ClassVO();

            Object document = ReflectUtil.getFieldValue(classDoc, "documentation");
            if (document != null) {
                String commentText = document.toString();
                for (String message : commentText.split("\n")) {
                    message = message.trim();
                    if (!message.startsWith("@") && message.length() > 0) {
                        classVO.setComment(message);
                        break;
                    }
                }
            }
            classVO.setType(classDoc.qualifiedName());
            classVO.setName(classDoc.simpleTypeName());

            List<FieldVO> fieldVOs = new ArrayList<>();

            FieldDoc[] fieldDocs = classDoc.fields(false);
            for (FieldDoc fieldDoc : fieldDocs) {
                if (!fieldDoc.isStatic()) {
                    FieldVO fieldVO = new FieldVO();
                    fieldVO.setComment(fieldDoc.commentText());

                    List<AnnotationVO> annotationVOs = new ArrayList<>();
                    AnnotationDesc[] annotationDescs = fieldDoc.annotations();
                    for (AnnotationDesc annotationDesc : annotationDescs) {
                        AnnotationVO annotationVO = new AnnotationVO();
                        annotationVO.setType(annotationDesc.annotationType().qualifiedTypeName());
                        annotationVOs.add(annotationVO);
                    }
                    fieldVO.setAnnotationVOs(annotationVOs);

                    fieldVO.setType(fieldDoc.type().qualifiedTypeName());
                    fieldVO.setName(fieldDoc.name());
                    fieldVOs.add(fieldVO);
                }
            }

            classVO.setFieldVOs(fieldVOs);
            classVOs.add(classVO);
        }

        return classVOs;
    }

}
