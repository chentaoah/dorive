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

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

@Data
@NoArgsConstructor
public class Result<E> {

    private Page<E> page;
    private List<E> records = Collections.emptyList();
    private E record;
    private int total = 0;

    public Result(Page<E> page) {
        this.page = page;
        this.records = page.getRecords();
        this.record = !records.isEmpty() ? records.get(0) : null;
        this.total = this.records.size();
    }

    public Result(List<E> records) {
        this.records = records;
        this.record = !records.isEmpty() ? records.get(0) : null;
        this.total = this.records.size();
    }

}
