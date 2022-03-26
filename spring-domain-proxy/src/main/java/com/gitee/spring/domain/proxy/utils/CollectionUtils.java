package com.gitee.spring.domain.proxy.utils;

import java.util.List;
import java.util.function.Consumer;

public class CollectionUtils {

    @SuppressWarnings("unchecked")
    public static <T> void forEach(Object object, Consumer<T> consumer) {
        if (object != null) {
            if (object instanceof List) {
                List<T> list = (List<T>) object;
                for (T item : list) {
                    consumer.accept(item);
                }
            } else {
                consumer.accept((T) object);
            }
        }
    }

}
