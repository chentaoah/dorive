package com.gitee.spring.domain.core3.entity.operation;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Operation {

    public static final int NONE = 0x00000000;
    public static final int INSERT = 0x00000001;
    public static final int UPDATE = 0x00000002;
    public static final int DELETE = 0x00000004;
    public static final int INSERT_OR_UPDATE = INSERT | UPDATE;
    public static final int UPDATE_OR_DELETE = UPDATE | DELETE;

    private int type;
    private Object entity;

    public Operation(int type, Object entity) {
        this.type = type;
        this.entity = entity;
    }

    public Operation(Object entity) {
        this.entity = entity;
    }

}
