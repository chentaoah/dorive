package com.gitee.dorive.core.impl;

import com.gitee.dorive.core.api.Operable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class OperableList<E> extends ArrayList<E> implements Operable<E> {

    private List<E> listToDelete = Collections.emptyList();

    public OperableList(Collection<? extends E> c) {
        super(c);
    }

    public OperableList(Collection<? extends E> c, Collection<E> listToDelete) {
        super(c);
        this.listToDelete = new ArrayList<>(listToDelete);
    }

    @Override
    public List<E> getListToDelete() {
        return listToDelete;
    }

    @Override
    public void clearListToDelete() {
        listToDelete.clear();
    }

}
