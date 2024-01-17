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

package com.gitee.dorive.event.impl;

import cn.hutool.core.util.ArrayUtil;
import com.gitee.dorive.event.api.EntityEventListener;
import com.gitee.dorive.event.entity.EntityEvent;
import com.gitee.dorive.event.entity.EntityListenerDef;
import com.gitee.dorive.event.entity.OperationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Data
@Slf4j
@NoArgsConstructor
@AllArgsConstructor
public class EntityEventListenerAdapter implements EntityEventListener {

    private EntityListenerDef entityListenerDef;
    private EntityEventListener entityEventListener;
    private Integer order;

    @Override
    public void onEntityEvent(EntityEvent entityEvent) {
        OperationType[] subscribeTo = entityListenerDef.getSubscribeTo();
        boolean isSubscribe = ArrayUtil.contains(subscribeTo, entityEvent.getOperationType());
        if (isSubscribe) {
            boolean afterCommit = entityListenerDef.isAfterCommit();
            if (afterCommit && TransactionSynchronizationManager.isActualTransactionActive()) {
                onEntityEventWhenTxActive(entityEvent);
            } else {
                doOnEntityEvent(entityEvent, entityListenerDef.getRollbackFor());
            }
        }
    }

    private void onEntityEventWhenTxActive(EntityEvent entityEvent) {
        try {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public int getOrder() {
                    return order;
                }

                @Override
                public void afterCommit() {
                    doOnEntityEvent(entityEvent, null);
                }
            });
        } catch (Exception e) {
            log.error("Transaction registration failed: " + e.getMessage(), e);
        }
    }

    private void doOnEntityEvent(EntityEvent entityEvent, Class<? extends Throwable>[] rollbackFor) {
        try {
            entityEventListener.onEntityEvent(entityEvent);

        } catch (Throwable t) {
            if (rollbackFor != null && rollbackFor.length > 0) {
                Class<? extends Throwable> throwType = t.getClass();
                for (Class<? extends Throwable> rollbackType : rollbackFor) {
                    if (rollbackType.isAssignableFrom(throwType)) {
                        throw t;
                    }
                }
            }
            log.error("Exception occurred in entity event listening!", t);
        }
    }

}
