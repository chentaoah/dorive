package com.gitee.dorive.env.impl;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.core.env.Environment;

import java.util.Properties;

@Data
@AllArgsConstructor
public class ConfigResolver {

    private Environment environment;

    public Properties resolveInstance(Object instance) {
        ConstructResolver constructResolver = new ConstructResolver(environment);
        constructResolver.resolveConstruct(instance);
        KeyValuesResolver resolver = new KeyValuesResolver(environment);
        return resolver.resolveProperties(instance);
    }

}
