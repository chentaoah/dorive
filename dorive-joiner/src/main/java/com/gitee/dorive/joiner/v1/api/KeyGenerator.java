package com.gitee.dorive.joiner.v1.api;

import com.gitee.dorive.base.v1.core.api.Context;

public interface KeyGenerator {

    String generate(Context context, Object entity);

}
