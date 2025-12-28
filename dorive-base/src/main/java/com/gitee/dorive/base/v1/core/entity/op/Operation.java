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

package com.gitee.dorive.base.v1.core.entity.op;

import com.gitee.dorive.base.v1.common.enums.RootControl;
import lombok.Data;

/**
 * 操作
 */
@Data
public class Operation {

    private RootControl rootControl = RootControl.UNCONTROLLED;

    public void includeRoot() {
        this.rootControl = RootControl.INCLUDE_ROOT;
    }

    public void switchRoot(boolean flag) {
        this.rootControl = flag ? RootControl.INCLUDE_ROOT : RootControl.IGNORE_ROOT;
    }

    public boolean isUncontrolled() {
        return rootControl == RootControl.UNCONTROLLED;
    }

    public boolean isIncludeRoot() {
        return rootControl == RootControl.INCLUDE_ROOT;
    }

    public boolean isNotIgnoreRoot() {
        return rootControl != RootControl.IGNORE_ROOT;
    }

}
