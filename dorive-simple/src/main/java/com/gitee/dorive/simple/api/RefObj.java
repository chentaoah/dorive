package com.gitee.dorive.simple.api;

import com.gitee.dorive.core.api.Context;
import com.gitee.dorive.core.api.ContextBuilder;

public interface RefObj {

    int select(Context context);

    int select(ContextBuilder builder);

    int insertOrUpdate(Context context);

    int insertOrUpdate(ContextBuilder builder);

    int delete(Context context);

    int delete(ContextBuilder builder);

}
