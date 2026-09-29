/*
 * Copyright 2026 Digital Engine
 * SPDX-License-Identifier: Apache-2.0
 */

package com.gitee.dorive.base.v1.binder.api;

import com.gitee.dorive.base.v1.binder.enums.JoinType;
import com.gitee.dorive.base.v1.executor.api.Context;
import com.gitee.dorive.base.v1.executor.entity.qry.Example;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface BinderExecutor {

    List<Binder> getStrongBinders();

    List<Binder> getValueRouteBinders();

    List<Binder> getValueFilterBinders();

    Map<String, List<Binder>> getMergedStrongBindersMap();

    Map<String, List<Binder>> getMergedValueRouteBindersMap();

    List<String> getSelfFields();

    JoinType getJoinType();

    List<Binder> getRootStrongBinders();

    boolean hasValueRouteBinders();

    void appendFilterCriteria(Context context, Example example);

    void appendFilterValue(Context context, Example example);

    void getBoundValue(Context context, Object rootEntity, Collection<?> entities);

    void setBoundId(Context context, Object rootEntity, Object entity);

}
