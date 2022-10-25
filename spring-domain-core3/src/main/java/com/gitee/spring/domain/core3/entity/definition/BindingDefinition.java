package com.gitee.spring.domain.core3.entity.definition;

import cn.hutool.core.bean.BeanUtil;
import com.gitee.spring.domain.core3.annotation.Binding;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.core.annotation.AnnotationUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class BindingDefinition {

    private String field;
    private String bindProp;
    private String bindCtx;
    private String alias;
    private String bindAlias;

    public static List<BindingDefinition> newBindingDefinitions(ElementDefinition elementDefinition) {
        List<BindingDefinition> bindingDefinitions = new ArrayList<>();
        for (Binding bindingAnnotation : elementDefinition.getBindingAnnotations()) {
            Map<String, Object> annotationAttributes = AnnotationUtils.getAnnotationAttributes(bindingAnnotation);
            bindingDefinitions.add(BeanUtil.copyProperties(annotationAttributes, BindingDefinition.class));
        }
        return bindingDefinitions;
    }

}
