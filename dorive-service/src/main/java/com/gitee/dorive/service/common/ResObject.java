package com.gitee.dorive.service.common;

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
