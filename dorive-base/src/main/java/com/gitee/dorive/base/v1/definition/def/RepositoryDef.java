/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.base.v1.definition.def;

import com.gitee.dorive.base.v1.definition.annotation.Event;
import com.gitee.dorive.base.v1.definition.annotation.Repository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.lang.reflect.AnnotatedElement;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RepositoryDef {
    private String value;
    private Class<?> dataSource;
    private Class<?> factory;
    private Class<?> deserializer;
    private Class<?> serializer;
    private Class<?>[] derived;
    private List<EventDef> events;
    private Class<?>[] queries;

    public static RepositoryDef fromElement(AnnotatedElement element) {
        Repository repository = AnnotatedElementUtils.getMergedAnnotation(element, Repository.class);
        if (repository != null) {
            RepositoryDef repositoryDef = new RepositoryDef();
            repositoryDef.setValue(repository.value());
            repositoryDef.setDataSource(repository.dataSource());
            repositoryDef.setFactory(repository.factory());
            repositoryDef.setDeserializer(repository.deserializer());
            repositoryDef.setSerializer(repository.serializer());
            repositoryDef.setDerived(repository.derived());

            // 事件嵌套注解
            List<EventDef> eventDefs = new ArrayList<>();
            for (Event event : repository.events()) {
                EventDef eventDef = new EventDef();
                eventDef.setSource(event.source());
                eventDef.setTarget(event.target());
                eventDef.setPublisher(event.publisher());
                eventDefs.add(eventDef);
            }
            repositoryDef.setEvents(eventDefs);

            repositoryDef.setQueries(repository.queries());
            return repositoryDef;
        }
        return null;
    }
}
