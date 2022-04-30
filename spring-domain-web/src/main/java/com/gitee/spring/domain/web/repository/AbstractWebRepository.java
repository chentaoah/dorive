package com.gitee.spring.domain.web.repository;

import com.gitee.spring.domain.coating.repository.AbstractChainRepository;
import com.gitee.spring.domain.web.annotation.EnableWeb;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.core.annotation.AnnotationUtils;

@Data
@EqualsAndHashCode(callSuper = false)
public abstract class AbstractWebRepository<E, PK> extends AbstractChainRepository<E, PK> {

    protected boolean enableWeb;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        EnableWeb enableWeb = AnnotationUtils.getAnnotation(this.getClass(), EnableWeb.class);
        this.enableWeb = enableWeb != null;
    }

}
