package com.gitee.dorive.web.advice;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.NumberUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.beans.PropertyEditorSupport;
import java.util.Date;

@RestControllerAdvice
public class ParameterControllerAdvice {

    @InitBinder
    public void initBinder(WebDataBinder webDataBinder) {
        webDataBinder.registerCustomEditor(Date.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) {
                if (StringUtils.isBlank(text)) {
                    return;
                }
                text = text.trim();
                Object value;
                boolean isNumber = NumberUtil.isNumber(text);
                if (isNumber) {
                    int multi = NumberUtil.pow(10, 13 - text.length()).intValue();
                    value = DateUtil.date(NumberUtil.parseLong(text) * multi);
                } else {
                    value = DateUtil.parseDateTime(text);
                }
                setValue(value);
            }
        });
    }

}
