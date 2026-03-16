package com.gitee.dorive.executor.v1.impl.builder;

import cn.hutool.core.util.ReflectUtil;
import com.baomidou.mybatisplus.core.toolkit.LambdaUtils;
import com.baomidou.mybatisplus.core.toolkit.support.LambdaMeta;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.gitee.dorive.base.v1.core.api.Options;
import com.gitee.dorive.base.v1.core.entity.ctx.DefaultOptions;
import com.gitee.dorive.base.v1.executor.api.Matcher;
import com.gitee.dorive.base.v1.executor.api.Selector;
import com.gitee.dorive.executor.v1.impl.matcher.LambdaMatcher;
import com.gitee.dorive.executor.v1.impl.matcher.NameMatcher;
import com.gitee.dorive.executor.v1.impl.matcher.TypeMatcher;
import com.gitee.dorive.executor.v1.impl.selector.DefaultSelector;
import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.reflection.property.PropertyNamer;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class Builder {
    private String[] names;
    private Class<?>[] types;
    private List<Field> fields;
    private String[] selectors;

    public Builder match(String... names) {
        this.names = names;
        return this;
    }

    public Builder match(Class<?>... types) {
        this.types = types;
        return this;
    }

    public <T> Builder match(SFunction<T, ?> function) {
        LambdaMeta meta = LambdaUtils.extract(function);
        Class<?> instantiatedClass = meta.getInstantiatedClass();
        String fieldName = PropertyNamer.methodToProperty(meta.getImplMethodName());
        java.lang.reflect.Field field = ReflectUtil.getField(instantiatedClass, fieldName);
        if (fields == null) {
            this.fields = new ArrayList<>(4);
        }
        fields.add(field);
        return this;
    }

    public Builder select(String... selectors) {
        this.selectors = selectors;
        return this;
    }

    public Options build() {
        Options options = new DefaultOptions();

        // Matcher
        Matcher matcher = null;
        if (names != null && names.length > 0) {
            matcher = new NameMatcher(names);

        } else if (types != null && types.length > 0) {
            if (types.length == 1 && (fields != null && !fields.isEmpty())) {
                matcher = new LambdaMatcher(types[0], fields);
            } else {
                matcher = new TypeMatcher(types);
            }
        }
        if (matcher != null) {
            options.setOption(Matcher.class, matcher);
        }

        // Selector
        if (selectors != null && selectors.length > 0) {
            options.setOptions(Selector.class, Arrays.stream(selectors).map(DefaultSelector::new).collect(Collectors.toList()));
        }

        return options;
    }
}
