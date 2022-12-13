package com.gitee.spring.boot.starter.dorive.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArgSegment {

    private String property;
    private String operator;
    private int index;

    @Override
    public String toString() {
        return property + " " + operator + " {" + index + "}";
    }

}
