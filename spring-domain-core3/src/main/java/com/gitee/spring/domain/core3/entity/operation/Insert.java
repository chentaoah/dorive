package com.gitee.spring.domain.core3.entity.operation;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Insert extends Operation {

    public Insert(int type, Object entity) {
        super(type, entity);
    }

    public Insert(Object entity) {
        super(entity);
    }

}
