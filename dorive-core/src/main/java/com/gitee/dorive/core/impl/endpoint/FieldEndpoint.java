package com.gitee.dorive.core.impl.endpoint;

import com.gitee.dorive.api.entity.ele.FieldElement;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FieldEndpoint extends SpELEndpoint {

    private String alias;

    public FieldEndpoint(FieldElement fieldElement, String expression) {
        super(fieldElement, expression);
    }

}
