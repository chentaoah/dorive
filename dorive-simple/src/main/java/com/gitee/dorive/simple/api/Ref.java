package com.gitee.dorive.simple.api;

import com.gitee.dorive.coating.api.CoatingRepository;
import com.gitee.dorive.core.api.ListableRepository;

public interface Ref<E> extends ListableRepository<E, Object>, CoatingRepository<E, Object>, SimpleRepository<E, Object> {

    RefObj forObj(E obj);

}
