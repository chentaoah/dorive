/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gitee.dorive.core.entity.executor;

import com.gitee.dorive.core.entity.operation.Query;
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
