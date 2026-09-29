/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.event.v1.impl.publisher.app;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.lang.NonNull;

@Getter
@Setter
@AllArgsConstructor
public class SourceEventPublisher implements ApplicationEventPublisher {

    private final Class<?> source;
    private final ApplicationEventPublisher publisher;

    @Override
    public void publishEvent(@NonNull Object event) {
        if (source.isAssignableFrom(event.getClass())) {
            publisher.publishEvent(event);
        }
    }

}
