package com.gitee.spring.domain.proxy.api;

public interface CustomAssembler<E> {

    void assembleBy(E entity);

    void disassembleTo(E entity);

}
