package com.gitee.spring.domain.coating.api;

public interface CoatingAssembler {

    void assemble(Object coatingObject, Object entity);

    void disassemble(Object coatingObject, Object entity);

}
