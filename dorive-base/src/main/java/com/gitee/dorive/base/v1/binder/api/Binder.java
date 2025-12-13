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

package com.gitee.dorive.base.v1.binder.api;

import com.gitee.dorive.base.v1.common.def.BindingDef;
import com.gitee.dorive.base.v1.core.api.Context;

public interface Binder extends Processor {

    BindingDef getBindingDef();

    String getBindField();

    String getFieldName();

    Object getFieldValue(Context context, Object entity);

    void setFieldValue(Context context, Object entity, Object value);

    String getBoundName();

    Object getBoundValue(Context context, Object entity);

    void setBoundValue(Context context, Object entity, Object value);

}
