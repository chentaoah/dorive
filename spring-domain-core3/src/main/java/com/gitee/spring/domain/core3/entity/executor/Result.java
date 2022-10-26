package com.gitee.spring.domain.core3.entity.executor;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

@Data
@NoArgsConstructor
public class Result {

    private List<Object> records = Collections.emptyList();
    private Page<Object> page;

    public Result(List<Object> records) {
        this.records = records;
    }

    public Result(Page<Object> page) {
        this.page = page;
        this.records = page.getRecords();
    }

    public Object getOne() {
        return !records.isEmpty() ? records.get(0) : null;
    }

}
