package com.gitee.spring.domain.event.listener;

import com.gitee.spring.domain.event.annotation.EntityListener;
import com.gitee.spring.domain.event.api.EventListener;
import com.gitee.spring.domain.core.entity.EntityDefinition;
import com.gitee.spring.domain.event.entity.RepositoryEvent;
import com.gitee.spring.domain.event.repository.EventRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.core.annotation.AnnotationUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class RepositoryListener implements ApplicationListener<RepositoryEvent>, ApplicationContextAware, InitializingBean {

    protected ApplicationContext applicationContext;
    protected Map<Class<?>, List<EventListener>> classEventListenerMap = new LinkedHashMap<>();

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() {
        Map<String, EventListener> entityListenerMap = applicationContext.getBeansOfType(EventListener.class);
        for (EventListener eventListener : entityListenerMap.values()) {
            EntityListener entityListener = AnnotationUtils.getAnnotation(eventListener.getClass(), EntityListener.class);
            if (entityListener != null) {
                Class<?> entityClass = entityListener.value();
                List<EventListener> eventListeners = classEventListenerMap.computeIfAbsent(entityClass, key -> new ArrayList<>());
                eventListeners.add(eventListener);
            }
        }
    }

    @Override
    public void onApplicationEvent(RepositoryEvent event) {
        EventRepository eventRepository = (EventRepository) event.getSource();
        EntityDefinition entityDefinition = eventRepository.getEntityDefinition();
        Class<?> entityClass = entityDefinition.getGenericEntityClass();
        List<EventListener> eventListeners = classEventListenerMap.get(entityClass);
        for (EventListener eventListener : eventListeners) {
            try {
                eventListener.onApplicationEvent(event);
            } catch (Exception e) {
                log.error("Exception occurred in event listening!", e);
            }
        }
    }

}
