package com.gitee.dorive.executor.v1.impl.builder;

import cn.hutool.core.util.ReflectUtil;
import com.baomidou.mybatisplus.core.toolkit.LambdaUtils;
import com.baomidou.mybatisplus.core.toolkit.support.LambdaMeta;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.gitee.dorive.base.v1.core.api.Options;
import com.gitee.dorive.base.v1.core.entity.ctx.DefaultOptions;
import com.gitee.dorive.base.v1.executor.api.Matcher;
import com.gitee.dorive.base.v1.executor.api.Selection;
import com.gitee.dorive.executor.v1.impl.matcher.LambdaMatcher;
import com.gitee.dorive.executor.v1.impl.matcher.NameMatcher;
import com.gitee.dorive.executor.v1.impl.matcher.TypeMatcher;
import com.gitee.dorive.executor.v1.impl.selector.DefaultSelection;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
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
    private String[] selections;

    public static Options build(String... names) {
        return new Builder().match(names).build();
    }

    public static Options build(Class<?>... types) {
        return new Builder().match(types).build();
    }

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

    public Builder select(String... selections) {
        this.selections = selections;
        return this;
    }

    public Options build() {
        Options options = new DefaultOptions();
        // Matcher
        Matcher matcher = null;
        List<Selection> matcherSelections = null;
        if (names != null && names.length > 0) {
            NameMatcher nameMatcher = new NameMatcher(names);
            matcher = nameMatcher;
            matcherSelections = nameMatcher.getSelections();

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
        if (matcherSelections != null) {
            options.setOptions(Selection.class, matcherSelections);
        }
        // Selection
        if (selections != null && selections.length > 0) {
            options.setOptions(Selection.class, Arrays.stream(selections).map(this::newSelection).collect(Collectors.toList()));
        }
        return options;
    }

    private Selection newSelection(String string) {
        return StringUtils.isNotBlank(string) ? new DefaultSelection(string) : null;
    }
}
