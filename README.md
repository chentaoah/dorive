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

基础设施层囊括了之前的持久层。

为了防止应用层跳过领域层，直接调用底层持久层服务，需要先进行领域划分，以此梳理出各个领域之间的关系。

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
```
