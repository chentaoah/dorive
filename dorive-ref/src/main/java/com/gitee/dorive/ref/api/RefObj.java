package com.gitee.dorive.ref.api;

import com.gitee.dorive.core.api.Context;
import com.gitee.dorive.core.api.Selector;

public interface RefObj {

    int select(Context context);

    int select(Selector selector);

    int insertOrUpdate(Context context);

    int insertOrUpdate(Selector selector);

    int delete(Context context);

    int delete(Selector selector);

}
