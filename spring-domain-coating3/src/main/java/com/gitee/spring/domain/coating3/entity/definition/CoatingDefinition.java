package com.gitee.spring.domain.coating3.entity.definition;

import cn.hutool.core.bean.BeanUtil;
import com.gitee.spring.domain.coating3.annotation.Coating;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.lang.reflect.AnnotatedElement;
import java.util.Map;

public class CoatingDefinition {

    public static CoatingDefinition newCoatingDefinition(AnnotatedElement annotatedElement) {
        Map<String, Object> annotationAttributes = AnnotatedElementUtils.getMergedAnnotationAttributes(annotatedElement, Coating.class);
        return BeanUtil.copyProperties(annotationAttributes, CoatingDefinition.class);
    }

}
