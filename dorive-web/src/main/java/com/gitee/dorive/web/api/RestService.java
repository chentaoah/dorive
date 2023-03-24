/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gitee.dorive.web.api;

import com.gitee.dorive.core.entity.executor.Page;
import com.gitee.dorive.web.common.ResObject;

import java.util.List;

public interface RestService<E, Q> {

    ResObject<Object> post(E entity);

    ResObject<List<E>> get(Q query);

    ResObject<Page<E>> page(Q query);

    ResObject<Object> put(Integer id, E entity);

    ResObject<Object> delete(Integer id);

}
