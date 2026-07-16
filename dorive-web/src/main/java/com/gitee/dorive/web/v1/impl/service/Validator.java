package com.gitee.dorive.web.v1.impl.service;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ReflectUtil;
import com.gitee.dorive.base.v1.common.entity.EntityElement;
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

    public <E> Validator(AbstractQueryRepository<E, Object> repository, Class<?> entityClass, Function<Result, Result> function) {
        this.repository = repository;
        this.uniqueConstraintFields = new ArrayList<>();
        for (Field field : ReflectUtil.getFields(entityClass)) {
            if (AnnotationUtil.hasAnnotation(field, UniqueConstraint.class)) {
                uniqueConstraintFields.add(field);
            }
        }
        this.function = function == null ? Function.identity() : function;
    }

    public ResObject<Object> validate(String method, Object entity) {
        List<Result> results = new ArrayList<>(1);
        if (!validateUniqueConstraint(method, entity)) {
            results.add(new Result(method, entity, UniqueConstraint.class.getSimpleName(), "重复的数据"));
        }
        if (!results.isEmpty()) {
            String message = results.stream() //
                    .map(function) //
                    .map(Result::getMessage) //
                    .collect(Collectors.joining("，")) + "。";
            return ResObject.failWith(message);
        }
        return ResObject.success();
    }

    public boolean validateUniqueConstraint(String method, Object entity) {
        if (CollUtil.isEmpty(uniqueConstraintFields)) {
            return true;
        }
        Example example = newExampleByUniqueConstraintFields(entity);
        if ("edit".equals(method)) {
            EntityElement entityElement = repository.getEntityElement();
            String primaryKey = entityElement.getPrimaryKey();
            example.ne(primaryKey, ReflectUtil.getFieldValue(entity, primaryKey));
        }
        return repository.selectCountByExample(Options.ROOT, example) == 0;
    }

    public Example newExampleByUniqueConstraintFields(Object entity) {
        Example example = new Example();
        for (Field field : uniqueConstraintFields) {
            example.eq(field.getName(), ReflectUtil.getFieldValue(entity, field));
        }
        return example;
    }

    @Data
    @AllArgsConstructor
    public static class Result {
        private String method;
        private Object entity;
        private String type;
        private String message;
    }
}
