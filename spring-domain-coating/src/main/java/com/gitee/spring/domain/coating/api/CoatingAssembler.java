package com.gitee.spring.domain.coating.api;

public interface CoatingAssembler {

    void assemble(Object coating, Object entity);

    void disassemble(Object coating, Object entity);

}
