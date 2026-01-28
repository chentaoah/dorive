package org.springframework.boot.test.context;

import org.springframework.test.context.ContextCustomizer;

import java.util.Set;

public class SpringBootTestArgsProxy {

    public static String[] get(Set<ContextCustomizer> contextCustomizers) {
        return SpringBootTestArgs.get(contextCustomizers);
    }

}
