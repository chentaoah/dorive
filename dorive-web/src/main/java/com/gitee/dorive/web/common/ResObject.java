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

package com.gitee.dorive.web.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResObject<T> {

    private int code;
    private String message;
    private T data;

    public static <T> ResObject<T> success() {
        return new ResObject<>(0, "success", null);
    }

    public static <T> ResObject<T> failure() {
        return new ResObject<>(-1, "failed", null);
    }

    public static <T> ResObject<T> successMsg(String message) {
        return new ResObject<>(0, message, null);
    }

    public static <T> ResObject<T> successData(T data) {
        return new ResObject<>(0, "success", data);
    }

    public static <T> ResObject<T> failMsg(String message) {
        return new ResObject<>(-1, message, null);
    }

    @JsonIgnore
    public boolean isSuccess() {
        return code == 0;
    }

    @JsonIgnore
    public boolean isFailed() {
        return code == -1;
    }

}
