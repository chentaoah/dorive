package com.gitee.spring.domain.core3.constant;

public interface OperationType {
    int NONE = 0x00000000;
    int INSERT = 0x00000001;
    int UPDATE = 0x00000002;
    int DELETE = 0x00000004;
    int INSERT_OR_UPDATE = INSERT | UPDATE;
    int UPDATE_OR_DELETE = UPDATE | DELETE;
}
