/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.base.v1.executor.entity.op;

import com.gitee.dorive.base.v1.executor.entity.qry.Example;
import com.gitee.dorive.base.v1.executor.entity.qry.Page;
import com.gitee.dorive.base.v1.executor.entity.cop.Query;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
public class Result<E> {

    private Page<E> page;
    private List<Map<String, Object>> recordMaps = Collections.emptyList();
    private List<E> records = Collections.emptyList();
    private E record;
    private long count = 0L;

    public static Result<Object> emptyResult(Query query) {
        Example example = query.getExample();
        if (example != null) {
            Page<Object> page = example.getPage();
            if (page != null) {
                return new Result<>(page);
            }
        }
        return new Result<>();
    }

    public Result(Page<E> page, List<Map<String, Object>> recordMaps) {
        this.page = page;
        this.recordMaps = recordMaps;
        this.count = this.recordMaps.size();
    }

    public Result(Page<E> page) {
        this.page = page;
        this.records = page.getRecords();
        this.record = !records.isEmpty() ? records.get(0) : null;
        this.count = this.records.size();
    }

    public Result(List<E> records) {
        this.records = records;
        this.record = !records.isEmpty() ? records.get(0) : null;
        this.count = this.records.size();
    }

    public Result(E record) {
        this.record = record;
        this.count = 1L;
    }

    public Result(long count) {
        this.count = count;
    }

}
