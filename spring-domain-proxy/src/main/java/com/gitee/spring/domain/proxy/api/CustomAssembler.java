package com.gitee.spring.domain.proxy.api;

public interface CustomAssembler {

    void assembleBy(Object entity);

    void disassembleTo(Object entity);

}
