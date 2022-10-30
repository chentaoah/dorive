package com.gitee.spring.domain.core3.entity.executor;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Insert extends Operation {

    private Object entity;

    public Insert(Object entity) {
        this.entity = entity;
    }

}
