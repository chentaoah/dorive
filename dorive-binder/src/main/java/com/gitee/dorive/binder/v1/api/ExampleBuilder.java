package com.gitee.dorive.binder.v1.api;

import com.gitee.dorive.base.v1.core.api.Context;
import com.gitee.dorive.base.v1.core.entity.qry.Example;

import java.util.List;

public interface ExampleBuilder {

    Example newExample(Context context, List<Object> entities);

}
