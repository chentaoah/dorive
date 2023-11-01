package com.gitee.dorive.api.entity.web;

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

    public static ResObject<Object> success() {
        return new ResObject<>(SUCCESS_CODE, SUCCESS_MSG, null);
    }

    public static ResObject<Object> fail() {
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
