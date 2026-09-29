/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
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
    public static final int FAILURE_CODE = -1;

    public static final String SUCCESS_MSG = "success";
    public static final String FAILURE_MSG = "failure";

    private int code;
    private String message;
    private T data;

    public static <T> ResObject<T> success() {
        return new ResObject<>(SUCCESS_CODE, SUCCESS_MSG, null);
    }

    public static <T> ResObject<T> failure() {
        return new ResObject<>(FAILURE_CODE, FAILURE_MSG, null);
    }

    public static <T> ResObject<T> success(T data) {
        return new ResObject<>(SUCCESS_CODE, SUCCESS_MSG, data);
    }

    public static <T> ResObject<T> failure(T data) {
        return new ResObject<>(FAILURE_CODE, FAILURE_MSG, data);
    }

    public static <T> ResObject<T> success(String message, T data) {
        return new ResObject<>(SUCCESS_CODE, message, data);
    }

    public static <T> ResObject<T> failure(String message, T data) {
        return new ResObject<>(FAILURE_CODE, message, data);
    }

    public static <T> ResObject<T> successWith(String message) {
        return new ResObject<>(SUCCESS_CODE, message, null);
    }

    public static <T> ResObject<T> failWith(String message) {
        return new ResObject<>(FAILURE_CODE, message, null);
    }

    public static <T> ResObject<T> of(boolean flag) {
        return flag ? success() : failure();
    }

    @JsonIgnore
    public boolean isSuccess() {
        return code == SUCCESS_CODE;
    }

    @JsonIgnore
    public boolean isFailure() {
        return code == FAILURE_CODE;
    }

}
