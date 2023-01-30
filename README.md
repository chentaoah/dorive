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

- 《领域驱动设计：软件核心复杂性应对之道》------ 作者：Eric Evans，领域驱动设计之父，世界杰出软件建模专家。
- 《实现领域驱动设计》------ 作者：Vaughn Vernon，经验丰富的软件工匠，在软件设计、开发和架构方面拥有超过25年的从业经验。

### 🙁面临的困境

- 业务知识的提炼和抽象
- 面向对象编程 + 数据持久化

### 😁功能点

| 功能点       | 描述                                                         |
| ------------ | ------------------------------------------------------------ |
| 依赖注入校验 | 依赖注入时，框架会校验Bean所属的领域是否一致。如果不一致，会给出警告。 |
| 模型声明     | 通过给Java实体类添加@Entity注解，声明一个实体。多个实体嵌套，组成一个聚合。<br />通过仓储，可以对聚合进行增、删、改、查。 |
| 事件通知     | 当仓储操作一个聚合时，框架会向外发送增、删、改事件。         |
| 实体动态构造 | 在构造实体时，可以根据实体的属性，构造出实体或者实体的子类。<br />从而，实现方法重写、以及动态级联查询。 |
| 表结构生成   | 根据实体，生成对应的建表语句。                               |

