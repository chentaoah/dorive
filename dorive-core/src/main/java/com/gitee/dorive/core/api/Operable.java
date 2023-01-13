package com.gitee.dorive.core.api;

import java.util.List;

public interface Operable<E> {

    List<E> getListToDelete();

    void clearListToDelete();

}
