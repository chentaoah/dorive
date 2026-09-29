/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.base.v1.executor.entity.eop;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class InsertOrUpdate extends EntityOp {

    private Insert insert;
    private Update update;

    public InsertOrUpdate(List<?> entities) {
        super(entities);
    }

    @Override
    public void setRoot(boolean root) {
        super.setRoot(root);
        if (insert != null) {
            insert.setRoot(root);
        }
        if (update != null) {
            update.setRoot(root);
        }
    }

    @Override
    public void setMatched(Boolean matched) {
        super.setMatched(matched);
        if (insert != null) {
            insert.setMatched(matched);
        }
        if (update != null) {
            update.setMatched(matched);
        }
    }

}
