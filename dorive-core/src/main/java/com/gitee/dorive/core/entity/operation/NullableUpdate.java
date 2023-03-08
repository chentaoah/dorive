package com.gitee.dorive.core.entity.operation;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class NullableUpdate extends Update {

    private Set<String> nullableSet;

    public NullableUpdate(int type, Object entity) {
        super(type, entity);
    }

}
