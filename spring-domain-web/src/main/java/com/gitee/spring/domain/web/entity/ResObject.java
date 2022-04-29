package com.gitee.spring.domain.web.entity;

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

    public static <T> ResObject<T> successMsg(String message) {
        return new ResObject<>(0, message, null);
    }

    public static <T> ResObject<T> successData(T data) {
        return new ResObject<>(0, "success", data);
    }

    public static <T> ResObject<T> failMsg(String message) {
        return new ResObject<>(-1, message, null);
    }

}
