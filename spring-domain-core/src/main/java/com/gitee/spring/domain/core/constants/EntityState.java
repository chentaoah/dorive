package com.gitee.spring.domain.core.constants;

public interface EntityState {
    int NONE = 0x00000000;
    int INSERT = 0x00000001;
    int UPDATE_SELECTIVE = 0x00000002;
    int UPDATE = 0x00000004;
    int DELETE = 0x00000008;
    int INSERT_OR_UPDATE = INSERT | UPDATE;
    int UPDATE_SELECTIVE_OR_UPDATE_OR_DELETE = UPDATE_SELECTIVE | UPDATE | DELETE;
    int FORCE_IGNORE = 0x00000010;
    int FORCE_INSERT = 0x00000010 | INSERT;
}
