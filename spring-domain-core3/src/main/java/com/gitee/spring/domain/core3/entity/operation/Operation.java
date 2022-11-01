package com.gitee.spring.domain.core3.entity.operation;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Operation {

    public static final int NONE = 0x00000000;
    public static final int SELECT = 0x00000001;
    public static final int INSERT = 0x00000002;
    public static final int UPDATE = 0x00000004;
    public static final int DELETE = 0x00000008;
    public static final int INSERT_OR_UPDATE = INSERT | UPDATE;
    public static final int UPDATE_OR_DELETE = UPDATE | DELETE;

    protected int type;
    protected Object entity;

}
