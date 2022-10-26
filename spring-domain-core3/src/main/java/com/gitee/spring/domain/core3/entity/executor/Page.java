package com.gitee.spring.domain.core3.entity.executor;

import lombok.Data;

import java.util.Collections;
import java.util.List;

@Data
public class Page<T> {
    private long total = 0;
    private long current = 1;
    private long size = 10;
    private List<T> records = Collections.emptyList();
}
