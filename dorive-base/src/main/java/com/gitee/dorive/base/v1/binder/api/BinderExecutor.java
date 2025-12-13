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

import com.gitee.dorive.base.v1.common.enums.JoinType;
import com.gitee.dorive.base.v1.core.api.Context;
import com.gitee.dorive.base.v1.core.entity.qry.Example;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface BinderExecutor {

    List<String> getSelfFields();

    JoinType getJoinType();

    boolean hasValueRouteBinders();

    List<Binder> getValueFilterBinders();

    Map<String, List<Binder>> getMergedStrongBindersMap();

    Map<String, List<Binder>> getMergedValueRouteBindersMap();

    void appendFilterValue(Context context, Example example);

    void getBoundValue(Context context, Object rootEntity, Collection<?> entities);

    void setBoundId(Context context, Object rootEntity, Object entity);

}
