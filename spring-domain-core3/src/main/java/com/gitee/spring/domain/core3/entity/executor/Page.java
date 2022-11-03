package com.gitee.spring.domain.core3.entity.executor;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Page<T> {

    private long total = 0;
    private long current = 1;
    private long size = 10;
    private List<T> records = Collections.emptyList();

    public Page(long current, long size) {
        this.current = current;
        this.size = size;
    }

    @Override
    public String toString() {
        return "LIMIT " + (current - 1) * size + ", " + size;
    }

}
