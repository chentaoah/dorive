<h1 align="center">Dorive</h1>
<h3 align="center">轻量级领域驱动框架</h3>
<p align="center">
  <img src="https://img.shields.io/github/license/chentaoah/dorive" alt="license">
  <img src="https://img.shields.io/github/v/release/chentaoah/dorive?display_name=tag&include_prereleases" alt="release">
  <img src="https://img.shields.io/github/commit-activity/y/chentaoah/dorive" alt="commit">
  <img src="https://img.shields.io/github/stars/chentaoah/dorive?color=%231890FF&style=flat-square" alt="stars">
</p>
<hr/>
###  📚简介

​		dorive是一个轻量的领域驱动式业务框架，它提供了诸多开箱即用的功能，旨在帮助开发者快速、便捷地在项目中应用领域驱动，并从中受益。

​		这些功能涵盖了依赖注入校验、依赖即用配置、实体定义与映射、级联查询与操作、实体多态、实体事件通知、复杂推导查询、ref关键字、复杂计数统计、表结构生成、数据库逆向生成、代码生成等，覆盖了大部分开发场景。

###  🎁名称由来

​		dorive = domain + driven 或 do + driven ，是原公司项目沉淀后的开源库。“do”表明了一种态度，只有付诸行动，才能有所收获。

###  🍺设计理念

​		dorive提供统一的api，帮助开发者从繁复的增删改查中解脱出来，从而把精力集中到业务逻辑开发中。理想状态下，开发者无需再编写sql语句，也将无惧于快速迭代导致的频繁改动。

### 📊架构设计

![avatar](https://gitee.com/digital-engine/dorive/raw/master/doc/img/framework.png)

###  🛠️模块说明

| 模块                       | 说明                                                 |
| -------------------------- | ---------------------------------------------------- |
| dorive-inject              | 实现了依赖注入校验                                   |
| dorive-env                 | 实现了依赖即用配置                                   |
| dorive-web                 | 提供了web开发时会用到的工具类                        |
| dorive-proxy               | 动态代理工具包                                       |
| dorive-api                 | 包含领域驱动实体定义规范                             |
| dorive-core                | 核心实现（实体定义与映射、级联查询与操作、实体多态） |
| dorive-event               | 实现了实体事件通知                                   |
| dorive-query               | 实现了复杂推导查询                                   |
| dorive-ref                 | 实现了ref关键字                                      |
| dorive-sql                 | 实现了复杂计数统计                                   |
| dorive-mybatis-plus        | 提供基于mybatis-plus的实现                           |
| dorive-spring-boot-starter | 依赖管理启动器                                       |

###  📦安装

```xml
<dependency>
    <groupId>com.gitee.digital-engine</groupId>
    <artifactId>dorive-spring-boot-starter</artifactId>
    <version>3.4.3.4</version>
</dependency>
```

