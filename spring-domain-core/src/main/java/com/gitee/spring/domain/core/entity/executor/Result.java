package com.gitee.spring.domain.core.entity.executor;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

@Data
@NoArgsConstructor
public class Result {

    private Object record;
    private List<Object> records = Collections.emptyList();
    private Page<Object> page;

    public Result(Object record) {
        this.record = record;
    }

    public Result(List<Object> records) {
        this.records = records;
        this.record = !records.isEmpty() ? records.get(0) : null;
    }

    public Result(Page<Object> page) {
        this.page = page;
        this.records = page.getRecords();
        this.record = !records.isEmpty() ? records.get(0) : null;
    }

}
