## Spring Domain

Spring体系下实现的领域驱动框架。核心功能：

- 依赖注入校验
- 实体映射组装
- 领域事件传播
- 防腐层映射与查询

## 依赖管理

### 示意图

![avatar](https://gitee.com/digital-engine/spring-domain/raw/master/static/img/layer.png)

领域驱动的分层方式，有别于传统的三层。分别为：表现层、应用层、领域层、基础设施层。

依赖管理包括两方面：

- 跨层级，不建议直接调用。
- 相同层级间，不建议直接调用内部服务。

### 配置

```yaml
spring:
  domain:
    enable: true
    scan: com.company.project.**        # 扫描包名
    domains:
      - name: dal                       # 一个名叫dal的领域
        pattern: com.company.project.dal.**
        protect: com.company.project.dal.mapper.** # 该领域内不建议外部直接调用的服务
      - name: dal-domain                # 一个名叫dal-domain的领域，它是dal的子域
        pattern: com.company.project.domain.**
      - name: serviceA                  # 一个名叫serviceA的领域
        pattern: com.company.project.service.a.**
      - name: serviceB                  # 一个名叫serviceB的领域
        pattern: com.company.project.service.b.**
        protect: com.company.project.service.b.internal.** # 该领域内不建议外部直接调用的服务
```
