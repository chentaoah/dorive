package com.gitee.spring.domain.core3.entity.definition;

import cn.hutool.core.bean.BeanUtil;
import com.gitee.spring.domain.core3.annotation.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.core.annotation.AnnotationUtils;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EntityDefinition {

    private String[] context;
    private Class<?> mapper;
    private String orderByAsc;
    private String orderByDesc;
    private String page;
    private int order;
    private Class<?> factory;
    private Class<?> repository;

    public static EntityDefinition newEntityDefinition(ElementDefinition elementDefinition) {
        Entity entityAnnotation = elementDefinition.getEntityAnnotation();
        Map<String, Object> annotationAttributes = AnnotationUtils.getAnnotationAttributes(entityAnnotation);
        return BeanUtil.copyProperties(annotationAttributes, EntityDefinition.class);
    }

}
