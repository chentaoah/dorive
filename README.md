<h1 align="center">Dorive</h1>
<h3 align="center">轻量级领域驱动框架</h3>
<p align="center">
  <img src="https://img.shields.io/github/license/chentaoah/dorive" alt="license">
  <img src="https://img.shields.io/github/v/release/chentaoah/dorive?display_name=tag&include_prereleases" alt="release">
  <img src="https://img.shields.io/github/commit-activity/y/chentaoah/dorive" alt="commit">
  <img src="https://img.shields.io/github/stars/chentaoah/dorive?color=%231890FF&style=flat-square" alt="stars">
</p>
<hr/>
🔥🔥🔥dorive轻量级领域驱动框架，帮助开发者通过建模，快速构建具有可维护性、可拓展性的应用程序。

### ❓领域驱动

如果你还不了解领域驱动设计，可以先阅读相关的书籍。

- 《领域驱动设计：软件核心复杂性应对之道》作者：Eric Evans
- 《实现领域驱动设计》作者：Vaughn Vernon

### 🙁面临的困境

- 业务知识的提炼和抽象
- 面向对象编程 + 数据持久化

### 😁功能点

| 功能点         | 描述                                                         |
| -------------- | ------------------------------------------------------------ |
| 依赖注入校验   | 依赖注入时，校验Bean所属的领域是否一致。如果不一致，会给出警告。 |
| 模型声明       | 启动时，将带有@Entity注解的Java类，解析为一个实体。多个实体嵌套，视为一个聚合。<br />通过仓储，可以对聚合，进行级联增、删、改、查操作。 |
| 事件通知       | 当仓储操作一个聚合时，会向外发送增、删、改事件。             |
| 实体动态构造   | 通过自定义工厂，可以实现动态构造实体对象。从而，实现方法重写、以及动态级联查询。 |
| 实体变更持久化 | 实体变更自身属性后，间接同步到数据库。                       |
| 表结构生成     | 根据实体，生成对应的建表语句。                               |

### 📖项目结构

| 模块                       | 描述                             |
| -------------------------- | -------------------------------- |
| dorive-injection           | 实现依赖注入校验                 |
| dorive-proxy               | 实现动态代理，取代反射           |
| dorive-core                | 实现实体解析、仓储CRUD的核心逻辑 |
| dorive-event               | 实现仓储操作时的事件通知机制     |
| dorive-coating             | 实现通过防腐层对象的查询机制     |
| dorive-spring-boot-starter | 实现与mybatis-plus的集成         |
| dorive-generator           | 实现根据实体生成数据库表结构     |

### 📊框架设计

![avatar](https://gitee.com/digital-engine/dorive/raw/master/doc/img/domain_model.png)