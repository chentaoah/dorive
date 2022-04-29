package com.gitee.spring.domain.web.repository;

import cn.hutool.core.util.StrUtil;
import com.gitee.spring.domain.coating.repository.AbstractChainRepository;
import com.gitee.spring.domain.web.annotation.EnableWeb;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.AnnotationUtils;

@Data
@EqualsAndHashCode(callSuper = false)
public abstract class AbstractWebRepository<E, PK> extends AbstractChainRepository<E, PK> {

    protected boolean enableWeb;
    protected String name;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        EnableWeb enableWeb = AnnotationUtils.getAnnotation(this.getClass(), EnableWeb.class);
        this.enableWeb = enableWeb != null;
        if (enableWeb != null) {
            this.name = enableWeb.name();
            if (StringUtils.isBlank(this.name)) {
                this.name = StrUtil.lowerFirst(entityClass.getSimpleName());
            }
        }
    }

}
