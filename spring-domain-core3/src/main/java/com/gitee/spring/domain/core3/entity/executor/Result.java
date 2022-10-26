package com.gitee.spring.domain.core3.entity.executor;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
public class Result {

    private List<Map<String, Object>> resultSet = Collections.emptyList();

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
