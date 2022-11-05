package com.gitee.spring.domain.event3.listener;

import com.gitee.spring.domain.core3.entity.definition.ElementDefinition;
import com.gitee.spring.domain.event3.annotation.Listener;
import com.gitee.spring.domain.event3.api.EntityListener;
import com.gitee.spring.domain.event3.repository.EventRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.annotation.AnnotationUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class RepositoryListenerV3 implements ApplicationListener<RepositoryEvent>, ApplicationContextAware, InitializingBean {

    private ApplicationContext applicationContext;

    private final Map<Class<?>, List<EntityListener>> classEventListenersMap = new LinkedHashMap<>();

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() {
        Map<String, EntityListener> entityListenerMap = applicationContext.getBeansOfType(EntityListener.class);
        List<EntityListener> entityListeners = new ArrayList<>(entityListenerMap.values());
        entityListeners.sort(new AnnotationAwareOrderComparator());
        for (EntityListener entityListener : entityListeners) {
            Listener listener = AnnotationUtils.getAnnotation(entityListener.getClass(), Listener.class);
            if (listener != null) {
                Class<?> entityClass = listener.value();
                List<EntityListener> existEntityListeners = classEventListenersMap.computeIfAbsent(entityClass, key -> new ArrayList<>());
                existEntityListeners.add(entityListener);
            }
        }
    }

    @Override
    public void onApplicationEvent(RepositoryEvent event) {
        EventRepository eventRepository = (EventRepository) event.getSource();
        ElementDefinition elementDefinition = eventRepository.getElementDefinition();
        Class<?> entityClass = elementDefinition.getGenericEntityClass();
        List<EntityListener> entityListeners = classEventListenersMap.get(entityClass);
        if (entityListeners != null && !entityListeners.isEmpty()) {
            for (EntityListener entityListener : entityListeners) {
                try {
                    entityListener.onApplicationEvent(event);
                } catch (Exception e) {
                    log.error("Exception occurred in event listening!", e);
                }
            }
        }
    }

}
