package com.gitee.dorive.generator.entity;

import cn.hutool.core.collection.CollUtil;
import lombok.Data;

import java.util.List;

@Data
public class FieldVO {
    private String comment;
    private List<AnnotationVO> annotationVOs;
    private String type;
    private String name;

    public boolean isAnnotationPresent(Class<?> annotationClass) {
        return CollUtil.findOne(annotationVOs, annotationVO -> annotationVO.getType().equals(annotationClass.getName())) != null;
    }

}
