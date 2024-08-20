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

package com.gitee.dorive.event.impl.listener.adapter;

import com.gitee.dorive.api.entity.event.def.ListenerDef;
import com.gitee.dorive.event.api.EntityEventListener;
import com.gitee.dorive.event.entity.EntityEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;

@Data
@Slf4j
@NoArgsConstructor
@AllArgsConstructor
public class TransactionListenerAdapter implements EntityEventListener {

    private Integer order;
    private ListenerDef listenerDef;
    private EntityEventListener entityEventListener;

    @Override
    public void onEntityEvent(EntityEvent entityEvent) {
        boolean isTxActive = listenerDef.isAfterCommit() && TransactionSynchronizationManager.isActualTransactionActive();
        if (!isTxActive) {
            handleEntityEvent(entityEvent, true);
        } else {
            handleEntityEventWhenTxActive(entityEvent);
        }
    }

    private void handleEntityEvent(EntityEvent entityEvent, boolean canThrowException) {
        try {
            entityEventListener.onEntityEvent(entityEvent);

        } catch (Throwable throwable) {
            if (canThrowException && matchThrowExceptions(throwable)) {
                throw throwable;
            }
            log.error("Exception occurred in entity event listening!", throwable);
        }
    }

    private boolean matchThrowExceptions(Throwable throwable) {
        List<Class<? extends Throwable>> throwExceptions = listenerDef.getThrowExceptions();
        if (throwExceptions != null && !throwExceptions.isEmpty()) {
            Class<? extends Throwable> throwableType = throwable.getClass();
            for (Class<? extends Throwable> throwExceptionType : throwExceptions) {
                if (throwExceptionType.isAssignableFrom(throwableType)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void handleEntityEventWhenTxActive(EntityEvent entityEvent) {
        try {
            TransactionSynchronizationManager.registerSynchronization(new TransactionInvoker(entityEvent));
        } catch (Exception e) {
            log.error("Transaction registration failed: " + e.getMessage(), e);
        }
    }

    /**
     * 实现Ordered接口，是为了兼容spring-boot的2.3.2版本与2.7.8版本
     */
    @AllArgsConstructor
    private class TransactionInvoker implements TransactionSynchronization, Ordered {

        private final EntityEvent entityEvent;

        @Override
        public int getOrder() {
            return order;
        }

        @Override
        public void afterCommit() {
            handleEntityEvent(entityEvent, false);
        }

    }

}
