package com.gitee.dorive.core.impl.endpoint;

import com.gitee.dorive.core.api.binder.Endpoint;
import com.gitee.dorive.api.entity.ele.FieldElement;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public abstract class AbstractEndpoint implements Endpoint {

    private FieldElement fieldElement;

    public String getAlias() {
        return fieldElement.getAlias();
    }

}
