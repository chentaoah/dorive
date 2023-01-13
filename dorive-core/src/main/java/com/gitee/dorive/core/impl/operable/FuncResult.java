package com.gitee.dorive.core.impl.operable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FuncResult {
    private boolean isSkip;
    private int totalCount;
}
