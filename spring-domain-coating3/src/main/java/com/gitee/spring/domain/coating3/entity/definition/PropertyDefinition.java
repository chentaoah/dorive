package com.gitee.spring.domain.coating3.entity.definition;

import cn.hutool.core.bean.BeanUtil;
import com.gitee.spring.domain.coating3.annotation.Property;
import lombok.Data;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.lang.reflect.AnnotatedElement;
import java.util.Map;

@Data
public class PropertyDefinition {

    private String location;
    private String alias;
    private String operator;
    private boolean ignore;

    public static PropertyDefinition newPropertyDefinition(AnnotatedElement annotatedElement) {
        Map<String, Object> annotationAttributes = AnnotatedElementUtils.getMergedAnnotationAttributes(annotatedElement, Property.class);
        return BeanUtil.copyProperties(annotationAttributes, PropertyDefinition.class);
    }

}
