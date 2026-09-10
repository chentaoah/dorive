package com.gitee.dorive.base.v1.factory.api.value;

public interface ValueDeserializer {

    Object deserialize(String name, Object value);

}
