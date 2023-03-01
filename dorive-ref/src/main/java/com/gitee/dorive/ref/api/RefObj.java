package com.gitee.dorive.ref.api;

import com.gitee.dorive.core.api.Context;

public interface RefObj {

    int select(Context context);
    
    int insertOrUpdate(Context context);

    int delete(Context context);


}
