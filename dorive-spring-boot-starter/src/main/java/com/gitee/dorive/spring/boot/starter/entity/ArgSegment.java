package com.gitee.dorive.spring.boot.starter.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArgSegment {

    private String property;
    private String operator;
    private Integer index;

    @Override
    public String toString() {
        if (index != null) {
            return property + " " + operator + " {" + index + "}";
        } else {
            return property + " " + operator;
        }
    }

}
