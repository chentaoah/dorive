package com.gitee.dorive.generator.entity;

import lombok.Data;

import java.util.List;

@Data
public class ClassVO {
    private String comment;
    private String type;
    private String name;
    private List<FieldVO> fieldVOs;
}
