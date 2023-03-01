package com.gitee.dorive.core.api;

import com.gitee.dorive.core.repository.CommonRepository;

import java.util.List;

public interface Selector {

    boolean matches(Context context, CommonRepository repository);

    List<String> selectColumns(Context context, CommonRepository repository);

}
