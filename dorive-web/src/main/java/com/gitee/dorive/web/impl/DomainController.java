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

package com.gitee.dorive.web.impl;

import com.gitee.dorive.web.entity.QueryContext;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@AllArgsConstructor
@RestController("/domain")
public class DomainController {

    private final DomainService domainService;

    @GetMapping("/list/{entityName}/{configId}")
    public void list(HttpServletRequest request, HttpServletResponse response,
                     @PathVariable String entityName, @PathVariable String configId,
                     @RequestParam Map<String, Object> params) throws Exception {
        QueryContext queryContext = new QueryContext();
        queryContext.setRequest(request);
        queryContext.setResponse(response);
        queryContext.setMethodName("list");
        queryContext.setEntityName(entityName);
        queryContext.setConfigId(configId);
        queryContext.setParams(params);
        domainService.executeQuery(queryContext);
    }

    @GetMapping("/page/{entityName}/{configId}")
    public void page(HttpServletRequest request, HttpServletResponse response,
                     @PathVariable String entityName, @PathVariable String configId,
                     @RequestParam Map<String, Object> params) throws Exception {
        QueryContext queryContext = new QueryContext();
        queryContext.setRequest(request);
        queryContext.setResponse(response);
        queryContext.setMethodName("page");
        queryContext.setEntityName(entityName);
        queryContext.setConfigId(configId);
        queryContext.setParams(params);
        domainService.executeQuery(queryContext);
    }

}
