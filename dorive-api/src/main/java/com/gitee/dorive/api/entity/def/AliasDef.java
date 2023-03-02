package com.gitee.dorive.api.entity.def;

import cn.hutool.core.bean.BeanUtil;
import com.gitee.dorive.api.annotation.Alias;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.lang.reflect.AnnotatedElement;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AliasDef {

    private String value;

    public static AliasDef fromElement(AnnotatedElement element) {
        if (element.isAnnotationPresent(Alias.class)) {
            Map<String, Object> attributes = AnnotatedElementUtils.getMergedAnnotationAttributes(element, Alias.class);
            return BeanUtil.copyProperties(attributes, AliasDef.class);
        }
        return null;
    }

}
