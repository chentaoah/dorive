package com.gitee.dorive.core.impl.observe;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ObservedResult {
    private int operationType;
    private int totalCount;
}
