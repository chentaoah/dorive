<h1 align="center">Dorive</h1>
<h3 align="center">轻量化、渐进式、领域驱动式开发框架</h3>
<p align="center">
  <img src="https://img.shields.io/github/license/chentaoah/dorive" alt="license">
  <img src="https://img.shields.io/github/v/release/chentaoah/dorive?display_name=tag&include_prereleases" alt="release">
  <img src="https://img.shields.io/github/commit-activity/y/chentaoah/dorive" alt="commit">
</p>
<hr/>

###  🎁简介

🔥🔥🔥dorive是一个轻量化、渐进式、领域驱动式开发框架，帮助开发者，开发**可持续演进的复杂应用**。 

dorive = domain + driven 或 do + driven ，是原公司项目沉淀后的开源库。

“do”表明了一种态度，只有付诸行动，才能有所收获。

### 🏗️领域驱动

领域驱动是一种软件开发方式。

解决痛点：

- **腐化**（代码组织混乱，逻辑互相渗透）
- **僵化**（直接依赖过多，修改困难）

实施方法：

- 通过**业务划界**，理清业务之间的关系。
- 通过**业务建模**，表达真实的业务逻辑。

核心：

- **高内聚**
- **低耦合**

### 📚架构实现

领域驱动的几种架构实现方式：

- 分层架构（Layered Architecture）
- 六边形架构（Hexagonal Architecture）
- 清洁架构（Clean Architecture）
- 事件驱动架构（Event-Driven Architecture, EDA）
- CQRS（命令查询职责分离）
- 微服务架构（Microservices + DDD）

- **3M架构（Microservices + Module + Model）**

3M架构：

- 简介：一种新的架构实现。在分层架构、微服务架构的基础上，提出将项目**模块化、模型化**，从而达到业务自由组合，灵活定制的效果。
- 逻辑架构图：[logic.png](https://gitee.com/digital-engine/dorive/blob/3.5.2/doc/img/logic.png)
- 物理架构图：[physical.png](https://gitee.com/digital-engine/dorive/blob/3.5.2/doc/img/physical.png)

### 📦模块化（module)

一个应用系统由一个或多个模块组成。

模块应具有以下特性：

- 每个模块都具备独立运行的能力。
- 每个模块都能单独测试。
- 允许同个模块不同版本，同时存在。
- 模块之间配置、命名空间隔离。
- 模块与模块的配置、脚本等生命周期一致。
- 模块可选择向外暴露的bean。
- 模块之间不直接依赖，可通过接口与事件进行交互。

### 🛠️模型化（model)

同个领域内的多个模块共享一个边界上下文。

边界上下文的作用：

- 以参数的形式，影响模型的行为。

模型应具有以下特性：

- 由仓储负责查询业务数据。
- 由工厂负责构建模型实例。
- 模型的持久化行为，能够被监听。
- 查询行为与存储引擎解耦。

###  💯推荐理由

dorive开发框架实现了模块化、模型化的全部特性，你可以有选择性地使用。

模块化、模型化的优势：

- 多人协同，效率高。
- 升级或回退，影响小。
- 调用简单，易维护。
- 无缝迁移，复用性好。
- 面向对象，拓展性好。
- 事件驱动，耦合度低。
- 持续演进，技术负债低。
- 代码生成，开发快。

### 🚅快速开始

Maven引入：

```xml
<dependency>
    <groupId>com.gitee.digital-engine</groupId>
    <artifactId>dorive-spring-boot-starter</artifactId>
    <version>3.5.1</version>
</dependency>
```

### 🌰项目案例

项目地址：[dorive-example](https://gitee.com/digital-engine/dorive-example)

### 📝说明文档

文档地址：[Gitee wiki](https://gitee.com/digital-engine/dorive/wikis/pages)

### 🌿版本说明

例如：3.5.1（格式A.B.C）

- A-架构版本：架构重构时增加，不保证兼容性。
- B-特性版本：新特性引入时增加。
- C-迭代版本：功能优化、bug修复时增加。

### 💬依赖项

| 依赖库                    | 版本   | 说明                |
| ------------------------- | ------ | ------------------- |
| spring-boot-starter-web   | 2.7.18 | spring boot web集成 |
| spring-tx                 | 5.3.31 | spring事务管理      |
| hutool-all                | 5.8.25 | 工具库              |
| mybatis-plus-boot-starter | 3.5.7  | 数据库框架          |

### 🤝兼容性

| 模块                       | 说明             | 模块   | 适配Spring Boot版本 |
| -------------------------- | ---------------- | ------ | ------------------- |
| dorive-api                 | 框架规范         |        |                     |
| dorive-module              | 模块化核心实现   | 模块化 | 2.7.18              |
| dorive-test                | 测试插件实现     | 模块化 | 2.7.18              |
| dorive-core                | 模型化核心实现   | 模型化 | 2.2.2 - 2.7.18      |
| dorive-event               | 事件通知实现     | 模型化 | 2.2.2 - 2.7.18      |
| dorive-query               | 关联查询实现     | 模型化 | 2.2.2 - 2.7.18      |
| dorive-ref                 | 仓储引用实现     | 模型化 | 2.2.2 - 2.7.18      |
| dorive-sql                 | 动态查询实现     | 模型化 | 2.2.2 - 2.7.18      |
| dorive-mybatis-plus        | mybatis-plus适配 | 模型化 | 2.2.2 - 2.7.18      |
| dorive-web                 | web开发适配      | 模型化 | 2.2.2 - 2.7.18      |
| dorive-autoconfigure       | 自动配置实现     |        |                     |
| dorive-spring-boot-starter | 启动器           |        |                     |

### 🐞bug反馈与建议

提交问题反馈请说明正在使用的JDK版本、dorive版本，以及依赖库版本。

页面地址：[Gitee issue](https://gitee.com/digital-engine/dorive/issues)

### 🙏🏻特别感谢

- [hutool](https://gitee.com/dromara/hutool/tree/v5-master/)
- [mybatis-plus](https://gitee.com/baomidou/mybatis-plus/tree/master/)



