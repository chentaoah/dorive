## Spring Domain

Spring体系下实现的领域驱动框架。核心功能：

- 依赖注入校验
- 实体映射组装
- 领域事件传播
- 防腐层映射与查询

## 领域定义

```yaml
spring:
  domain:
    enable: true
    scan: com.company.project.** # 扫描包名
    domains:
      - name: core # 领域名称
        pattern: com.company.project.core.** # 该领域对应的包名
        protect: com.company.project.core.internal.** # 该领域内不能添加@Root的类型
      - name: core-extension # 领域名称（core领域的子域）
        pattern: com.company.project.core.extension.**
```

![avatar](https://gitee.com/digital-engine/spring-domain/raw/master/static/img/domain.png)

