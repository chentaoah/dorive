package com.gitee.dorive.core.impl.operable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OperationResult {
    private int operationType;
    private int totalCount;
}
