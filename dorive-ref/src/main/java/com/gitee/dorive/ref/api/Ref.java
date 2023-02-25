package com.gitee.dorive.ref.api;

import com.gitee.dorive.coating.api.CoatingRepository;
import com.gitee.dorive.core.api.Repository;

public interface Ref extends Repository<Object, Object>, CoatingRepository<Object, Object> {

    RefObj forObj(Object obj);

}
