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

package com.gitee.dorive.web.v1.entity;

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

    public static final int SUCCESS_CODE = 0;
    public static final int FAIL_CODE = -1;

    public static final String SUCCESS_MSG = "success";
    public static final String FAIL_MSG = "fail";

    private int code;
    private String message;
    private T data;

    public static <T> ResObject<T> success() {
        return new ResObject<>(SUCCESS_CODE, SUCCESS_MSG, null);
    }

    public static <T> ResObject<T> fail() {
        return new ResObject<>(FAIL_CODE, FAIL_MSG, null);
    }

    public static <T> ResObject<T> successMsg(String message) {
        return new ResObject<>(SUCCESS_CODE, message, null);
    }

    public static <T> ResObject<T> failMsg(String message) {
        return new ResObject<>(FAIL_CODE, message, null);
    }

    public static <T> ResObject<T> successData(T data) {
        return new ResObject<>(SUCCESS_CODE, SUCCESS_MSG, data);
    }

    public static <T> ResObject<T> failData(T data) {
        return new ResObject<>(FAIL_CODE, FAIL_MSG, data);
    }

    @JsonIgnore
    public boolean isSuccess() {
        return code == SUCCESS_CODE;
    }

    @JsonIgnore
    public boolean isFail() {
        return code == FAIL_CODE;
    }

}
