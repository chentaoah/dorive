package com.gitee.spring.domain.event.listener;

import com.gitee.spring.domain.event.annotation.EventListener;
import com.gitee.spring.domain.event.api.EntityListener;
import com.gitee.spring.domain.core.entity.EntityDefinition;
import com.gitee.spring.domain.event.entity.RepositoryEvent;
import com.gitee.spring.domain.core.repository.DefaultRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class RepositoryListener implements ApplicationListener<RepositoryEvent>, ApplicationContextAware, InitializingBean {

    protected ApplicationContext applicationContext;
    protected Map<Class<?>, List<EntityListener>> classEntityListenerMap = new LinkedHashMap<>();

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() {
        Map<String, EntityListener> entityListenerMap = applicationContext.getBeansOfType(EntityListener.class);
        for (EntityListener entityListener : entityListenerMap.values()) {
            EventListener eventListener = entityListener.getClass().getAnnotation(EventListener.class);
            if (eventListener != null) {
                Class<?> entityClass = eventListener.value();
                List<EntityListener> entityListeners = classEntityListenerMap.computeIfAbsent(entityClass, key -> new ArrayList<>());
                entityListeners.add(entityListener);
            }
        }
    }

    @Override
    public void onApplicationEvent(RepositoryEvent event) {
        if (event.getSource() instanceof DefaultRepository) {
            DefaultRepository defaultRepository = (DefaultRepository) event.getSource();
            EntityDefinition entityDefinition = defaultRepository.getEntityDefinition();
            Class<?> entityClass = entityDefinition.getGenericEntityClass();
            List<EntityListener> entityListeners = classEntityListenerMap.get(entityClass);
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
