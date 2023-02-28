package com.gitee.dorive.ref.api;

import com.gitee.dorive.coating.api.CoatingRepository;
import com.gitee.dorive.core.api.Repository;

public interface Ref<E> extends Repository<E, Object>, CoatingRepository<E, Object> {

    RefObj forObj(E obj);

}
