package com.gitee.spring.domain.web.controller;

import cn.hutool.core.lang.Assert;
import com.alibaba.fastjson.JSON;
import com.gitee.spring.domain.coating.api.CoatingAssembler;
import com.gitee.spring.domain.coating.entity.CoatingDefinition;
import com.gitee.spring.domain.coating.property.DefaultCoatingAssembler;
import com.gitee.spring.domain.core.api.EntityMapper;
import com.gitee.spring.domain.core.entity.BoundedContext;
import com.gitee.spring.domain.core.repository.ConfiguredRepository;
import com.gitee.spring.domain.core.utils.ReflectUtils;
import com.gitee.spring.domain.web.entity.ResObject;
import com.gitee.spring.domain.web.repository.AbstractWebRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RequestMapping("/domain/{repository}")
public class RepositoryController implements ApplicationContextAware, InitializingBean {

    private ApplicationContext applicationContext;
    private final Map<String, AbstractWebRepository<Object, Object>> nameRepositoryMap = new ConcurrentHashMap<>();

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    @SuppressWarnings("all")
    public void afterPropertiesSet() throws Exception {
        Map<String, AbstractWebRepository> beansOfType = applicationContext.getBeansOfType(AbstractWebRepository.class);
        for (AbstractWebRepository<Object, Object> abstractWebRepository : beansOfType.values()) {
            if (abstractWebRepository.isEnableWeb()) {
                String name = abstractWebRepository.getName();
                Assert.isTrue(!nameRepositoryMap.containsKey(name), "The same repository name cannot exist!");
                nameRepositoryMap.putIfAbsent(name, abstractWebRepository);
            }
        }
    }

    @PostMapping("/insert/{coating}")
    public ResObject<Object> insert(@PathVariable("repository") String repository, @PathVariable("coating") String coating, @RequestBody String message) {
        AbstractWebRepository<Object, Object> abstractWebRepository = nameRepositoryMap.get(repository);
        if (abstractWebRepository == null) {
            return ResObject.failMsg("The repository does not exist!");
        }
        if (StringUtils.isNotBlank(coating)) {
            Map<String, CoatingAssembler> nameCoatingAssemblerMap = abstractWebRepository.getNameCoatingAssemblerMap();
            CoatingAssembler coatingAssembler = nameCoatingAssemblerMap.get(coating);
            if (coatingAssembler == null) {
                return ResObject.failMsg("The coating does not exist!");
            }
            if (coatingAssembler instanceof DefaultCoatingAssembler) {
                CoatingDefinition coatingDefinition = ((DefaultCoatingAssembler) coatingAssembler).getCoatingDefinition();
                Object coatingObject = JSON.parseObject(message, coatingDefinition.getCoatingClass());
                Object entity = ReflectUtils.newInstance(abstractWebRepository.getEntityCtor(), null);
                abstractWebRepository.disassemble(coatingObject, entity);
                int count = abstractWebRepository.insert(entity);
                return ResObject.successData(count);
            }
        } else {
            Object entity = JSON.parseObject(message, abstractWebRepository.getEntityClass());
            int count = abstractWebRepository.insert(entity);
            return ResObject.successData(count);
        }
        return ResObject.failMsg("The server cannot process the request!");
    }

    @PostMapping("/selectPage/{coating}/{pageNum}/{pageSize}")
    public ResObject<Object> select(@PathVariable("repository") String repository, @PathVariable("coating") String coating,
                                    @PathVariable("pageNum") Integer pageNum, @PathVariable("pageSize") Integer pageSize,
                                    @RequestBody String message) {
        pageNum = pageNum == null || pageNum <= 0 ? 1 : pageNum;
        pageSize = pageSize == null || pageSize > 100 ? 10 : pageSize;

        AbstractWebRepository<Object, Object> abstractWebRepository = nameRepositoryMap.get(repository);
        if (abstractWebRepository == null) {
            return ResObject.failMsg("The repository does not exist!");
        }
        if (StringUtils.isNotBlank(coating)) {
            Map<String, CoatingAssembler> nameCoatingAssemblerMap = abstractWebRepository.getNameCoatingAssemblerMap();
            CoatingAssembler coatingAssembler = nameCoatingAssemblerMap.get(coating);
            if (coatingAssembler == null) {
                return ResObject.failMsg("The coating does not exist!");
            }
            if (coatingAssembler instanceof DefaultCoatingAssembler) {
                CoatingDefinition coatingDefinition = ((DefaultCoatingAssembler) coatingAssembler).getCoatingDefinition();
                Object coatingObject = JSON.parseObject(message, coatingDefinition.getCoatingClass());
                BoundedContext boundedContext = new BoundedContext();
                Object example = abstractWebRepository.buildExample(boundedContext, coatingObject);
                ConfiguredRepository rootConfiguredRepository = abstractWebRepository.getRootRepository();
                EntityMapper entityMapper = rootConfiguredRepository.getEntityMapper();
                Object pageInfo = entityMapper.newPage(pageNum, pageSize);
                Object dataPage = abstractWebRepository.selectPageByExample(boundedContext, example, pageInfo);
                return ResObject.successData(dataPage);
            }
        }
        return ResObject.failMsg("The server cannot process the request!");
    }

    @PostMapping("/update/{coating}")
    public ResObject<Object> update(@PathVariable("repository") String repository, @PathVariable("coating") String coating, @RequestBody String message) {
        AbstractWebRepository<Object, Object> abstractWebRepository = nameRepositoryMap.get(repository);
        if (abstractWebRepository == null) {
            return ResObject.failMsg("The repository does not exist!");
        }
        if (StringUtils.isNotBlank(coating)) {
            Map<String, CoatingAssembler> nameCoatingAssemblerMap = abstractWebRepository.getNameCoatingAssemblerMap();
            CoatingAssembler coatingAssembler = nameCoatingAssemblerMap.get(coating);
            if (coatingAssembler == null) {
                return ResObject.failMsg("The coating does not exist!");
            }
            if (coatingAssembler instanceof DefaultCoatingAssembler) {
                CoatingDefinition coatingDefinition = ((DefaultCoatingAssembler) coatingAssembler).getCoatingDefinition();
                Object coatingObject = JSON.parseObject(message, coatingDefinition.getCoatingClass());
                Object entity = ReflectUtils.newInstance(abstractWebRepository.getEntityCtor(), null);
                abstractWebRepository.disassemble(coatingObject, entity);
                int count = abstractWebRepository.update(entity);
                return ResObject.successData(count);
            }
        } else {
            Object entity = JSON.parseObject(message, abstractWebRepository.getEntityClass());
            int count = abstractWebRepository.update(entity);
            return ResObject.successData(count);
        }
        return ResObject.failMsg("The server cannot process the request!");
    }

    @GetMapping("/delete/{id}")
    public ResObject<Object> delete(@PathVariable("repository") String repository, @PathVariable("id") Integer id) {
        AbstractWebRepository<Object, Object> abstractWebRepository = nameRepositoryMap.get(repository);
        if (abstractWebRepository == null) {
            return ResObject.failMsg("The repository does not exist!");
        }
        int count = abstractWebRepository.deleteByPrimaryKey(id);
        return ResObject.successData(count);
    }

}
