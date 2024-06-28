package com.gitee.dorive.core.impl.endpoint;

import com.gitee.dorive.api.entity.ele.FieldElement;
import com.gitee.dorive.core.repository.CommonRepository;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BindEndpoint extends SpELEndpoint {

    private String belongAccessPath;
    private CommonRepository belongRepository;
    private String bindFieldAlias;

    public BindEndpoint(FieldElement fieldElement, String expression) {
        super(fieldElement, expression);
    }

}
