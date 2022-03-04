# spring-domain

在Spring体系下，领域驱动设计的具体实现。

![avatar](https://www.processon.com/embed/5fb61ce25653bb29a8007720)

## 依赖注入

在领域驱动设计中，各领域之间高内聚、低耦合。组件提供，在依赖注入时，进行领域校验的功能。

### 配置

```yaml
spring:
  domain:
    enable: true
    scan: com.company.project.** # 扫描包名
    domains:
      - name: model # 领域名称
        pattern: com.company.project.model.** # 该领域对应的包名
        protect: com.company.project.model.mapper.** # 该领域受保护类型
      - name: model-domian # 领域名称（model领域的子域）
        pattern: com.company.project.domain.**
```

