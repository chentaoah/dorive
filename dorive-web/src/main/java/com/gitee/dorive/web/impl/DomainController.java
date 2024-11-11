package com.gitee.dorive.web.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.gitee.dorive.core.api.context.Selector;
import com.gitee.dorive.query.repository.AbstractQueryRepository;
import com.gitee.dorive.web.entity.EntityQueryConfig;
import com.gitee.dorive.web.entity.ResObject;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
@RestController("/domain")
public class DomainController {

    private final ApplicationContext applicationContext;
    private Map<String, EntityQueryConfig> idConfigMap;

    @GetMapping("/list/{configId}")
    public void list(HttpServletResponse response,
                     @PathVariable String configId,
                     @RequestParam Map<String, Object> params) throws Exception {
        EntityQueryConfig entityQueryConfig = idConfigMap.get(configId);
        if (entityQueryConfig == null) {
            ResObject<?> resObject = ResObject.failMsg("没有找到配置信息！");
            response.getWriter().write(JSONUtil.toJsonStr(resObject));
            return;
        }

        Class<?> repositoryClass = entityQueryConfig.getRepositoryClass();
        AbstractQueryRepository<?, ?> repository = (AbstractQueryRepository<?, ?>) applicationContext.getBean(repositoryClass);

        Class<?> queryClass = entityQueryConfig.getQueryClass();
        Object query = BeanUtil.toBean(params, queryClass);

        Class<?> entityClass = entityQueryConfig.getEntityClass();
        String selectorName = entityQueryConfig.getSelectorName();
        Selector selector = findSelectorByName(entityClass, selectorName);
        List<?> entities = repository.selectByQuery(selector, query);
        ResObject<List<?>> resObject = ResObject.successData(entities);

        ObjectMapper objectMapper = new ObjectMapper();
        addFilters(objectMapper, selector);
        objectMapper.writeValue(response.getOutputStream(), resObject);
    }

    @GetMapping("/page/{configId}")
    public void page(HttpServletResponse response,
                     @PathVariable String configId,
                     @RequestParam Map<String, Object> params) {

    }

    private Selector findSelectorByName(Class<?> entityClass, String selectorName) {
        return null;
    }

    private void addFilters(ObjectMapper objectMapper, Selector selector) {
        objectMapper.setSerializerFactory(new MyBeanSerializerFactory());

        SimpleFilterProvider simpleFilterProvider = new SimpleFilterProvider();
        simpleFilterProvider.addFilter("tenantFilter", SimpleBeanPropertyFilter.filterOutAllExcept("tenantCode", "departments"));
        simpleFilterProvider.addFilter("deptFilter", SimpleBeanPropertyFilter.filterOutAllExcept("deptCode"));
        objectMapper.setFilterProvider(simpleFilterProvider);
    }

}
