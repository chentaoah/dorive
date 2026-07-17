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

package com.gitee.dorive.web.v1.impl.service;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ReflectUtil;
import com.gitee.dorive.base.v1.core.api.Options;
import com.gitee.dorive.base.v1.core.entity.qry.Example;
import com.gitee.dorive.base.v1.web.annotation.UniqueConstraint;
import com.gitee.dorive.repository.v1.impl.repository.AbstractQueryRepository;
import com.gitee.dorive.web.v1.entity.ResObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
@Setter
public class Validator {

    private final AbstractQueryRepository<?, ?> repository;
    private final List<Field> uniqueConstraintFields;
    private final Function<Result, Result> function;
    private final boolean enabled;

    public <E> Validator(AbstractQueryRepository<E, Object> repository, Class<?> entityClass, Function<Result, Result> function) {
        this.repository = repository;
        this.uniqueConstraintFields = new ArrayList<>(4);
        for (Field field : ReflectUtil.getFields(entityClass)) {
            if (AnnotationUtil.hasAnnotation(field, UniqueConstraint.class)) {
                uniqueConstraintFields.add(field);
            }
        }
        this.function = function == null ? Function.identity() : function;
        this.enabled = CollUtil.isNotEmpty(uniqueConstraintFields);
    }

    public ResObject<Object> validate(String method, Object entity) {
        if (enabled) {
            List<Result> results = new ArrayList<>(1);
            int code = validateUniqueConstraint(method, entity);
            if (code == -1) { // 提交数据已存在
                String fieldsMsg = uniqueConstraintFields.stream().map(Field::getName).collect(Collectors.joining("、"));
                String message = String.format("提交数据已存在，请检查字段：%s", fieldsMsg);
                results.add(new Result(method, entity, uniqueConstraintFields, UniqueConstraint.class.getSimpleName(), message));

            } else if (code == -2) { // 提交数据字段为空
                String fieldsMsg = uniqueConstraintFields.stream().map(Field::getName).collect(Collectors.joining("、"));
                String message = String.format("提交数据字段为空，请检查字段：%s", fieldsMsg);
                results.add(new Result(method, entity, uniqueConstraintFields, UniqueConstraint.class.getSimpleName(), message));
            }
            if (!results.isEmpty()) {
                String message = results.stream() //
                        .map(function) //
                        .map(Result::getMessage) //
                        .collect(Collectors.joining("；"));
                return ResObject.failWith(message);
            }
        }
        return ResObject.success();
    }

    public int validateUniqueConstraint(String method, Object entity) {
        if (CollUtil.isEmpty(uniqueConstraintFields)) {
            return 0;
        }
        int nullCount = 0;
        Example example = new Example();
        for (Field field : uniqueConstraintFields) {
            String fieldName = field.getName();
            Object fieldValue = ReflectUtil.getFieldValue(entity, field);
            if (fieldValue == null) {
                nullCount++;
            }
            example.eq(fieldName, fieldValue);
        }
        if (nullCount > 0) {
            return "edit".equals(method) && nullCount == uniqueConstraintFields.size() ? 0 : -2;
        }
        if ("edit".equals(method)) {
            String fieldName = repository.getEntityElement().getPrimaryKey();
            Object fieldValue = ReflectUtil.getFieldValue(entity, fieldName);
            if (fieldValue == null) {
                return -2;
            }
            example.ne(fieldName, fieldValue);
        }
        return repository.selectCountByExample(Options.ROOT, example) == 0 ? 0 : -1;
    }

    @Data
    @AllArgsConstructor
    public static class Result {
        private String method;
        private Object entity;
        private List<Field> fields;
        private String type;
        private String message;
    }
}
