<h1 align="center">Dorive</h1>
<h3 align="center">轻量化、渐进式、领域驱动式开发框架</h3>
<p align="center">
  <img src="https://img.shields.io/github/license/chentaoah/dorive" alt="license">
  <img src="https://img.shields.io/github/v/release/chentaoah/dorive?display_name=tag&include_prereleases" alt="release">
  <img src="https://img.shields.io/github/commit-activity/y/chentaoah/dorive" alt="commit">
  <img src="https://img.shields.io/github/stars/chentaoah/dorive?color=%231890FF&style=flat-square" alt="stars">
</p>
<hr/>

###  🎁简介

🔥🔥🔥dorive是一个轻量化、渐进式开发框架，帮助开发者，快速落地领域驱动式项目。 

dorive = domain + driven 或 do + driven ，是原公司项目沉淀后的开源库。

“do”表明了一种态度，只有付诸行动，才能有所收获。

### 📚3M架构

领域驱动设计的核心是高内聚、低耦合。为了实现它，现代应用架构正朝着微服务化、模块化、模型化方向发展。

3M架构 =  Microservices + Module + Model

### 📦模块化

一个应用系统由一个或多个模块组成。

模块应具有以下特性：

- 每个模块都具备独立运行的能力。
- 每个模块都能单独测试。
- 模块之间配置、命名空间隔离。
- 模块可选择向外暴露的bean。
- 模块之间不直接依赖，可通过接口与事件进行交互。

### 🛠️模型化

一个模块由一个边界上下文与一个或多个模型组成。

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

- 多个模块互不影响，多人协同开发项目，效率高。
- 单个模块升级或回退，影响小。
- 模块间调用关系简单，易维护。
- 模块可嵌入其他项目，复用性好。
- 模块提供接口，重写业务逻辑，拓展性好。
- 建模后，可自动生成基本代码与脚本。
- 可重写模型方法，定制业务逻辑。
- 可监听模型事件，减少代码耦合度。

### 💬使用建议

基础依赖说明：

| 依赖库                    | 版本   | 说明                |
| ------------------------- | ------ | ------------------- |
| spring-boot-starter-web   | 2.7.18 | spring boot web集成 |
| spring-tx                 | 5.3.31 | spring事务管理      |
| hutool-all                | 5.8.25 | 工具库              |
| mybatis-plus-boot-starter | 3.5.7  | 数据库框架          |

如果是新项目，你将毫无负担地使用它：

```xml
<dependency>
    <groupId>com.gitee.digital-engine</groupId>
    <artifactId>dorive-spring-boot-starter</artifactId>
    <version>3.5.0.8</version>
</dependency>
```

如果是存量项目，你还需要考虑兼容性：

|  所属  | 模块                       | 说明             | 适配Spring Boot版本 |
| :----: | -------------------------- | ---------------- | ------------------- |
| 模块化 | dorive-module              | 模块化核心实现   | 2.7.18              |
| 模块化 | dorive-test                | 测试插件实现     | 2.7.18              |
| 模型化 | dorive-api                 | 领域建模范式     | 2.2.2 - 2.7.18      |
| 模型化 | dorive-core                | 模型化核心实现   | 2.2.2 - 2.7.18      |
| 模型化 | dorive-event               | 事件通知实现     | 2.2.2 - 2.7.18      |
| 模型化 | dorive-query               | 关联查询实现     | 2.2.2 - 2.7.18      |
| 模型化 | dorive-ref                 | 仓储引用实现     | 2.2.2 - 2.7.18      |
| 模型化 | dorive-sql                 | 动态查询实现     | 2.2.2 - 2.7.18      |
| 模型化 | dorive-mybatis-plus        | mybatis-plus适配 | 2.2.2 - 2.7.18      |
| 模型化 | dorive-web                 | web开发适配      | 2.2.2 - 2.7.18      |
|        | dorive-spring-boot-starter | 启动器           | 2.2.2 - 2.7.18      |

### 📝文档

wiki地址：

- [Gitee wiki](https://gitee.com/digital-engine/dorive/wikis/pages)

### 🐞bug反馈与建议

提交问题反馈请说明正在使用的JDK版本、dorive版本，以及依赖库版本。

- [Gitee issue](https://gitee.com/digital-engine/dorive/issues)

### 📘版本说明

| 版本 | 说明                                                         |
| ---- | ------------------------------------------------------------ |
| 1.x  | 试验领域驱动落地的可能性                                     |
| 2.x  | 完成基本功能开发，并在项目中应用                             |
| 3.x  | 重新设计内部架构，并优化大量代码。自3.4.3.4版本开始，项目进入维护阶段 |

### 🌿分支说明

| 分支   | 说明                                                         |
| ------ | ------------------------------------------------------------ |
| master | 主分支，release版本使用的分支，不接收任何pr或修改            |
| 3.x    | 版本分支，分支名即版本号，版本号高于master的，为正在开发的分支 |

### 🤝与我（们）一起

1. 请将个人联系方式，发送邮件至digital_engine@163.com
2. 等待维护者向你发出邀请

###  ⭐Star dorive

[![Stargazers over time](https://starchart.cc/chentaoah/dorive.svg?variant=adaptive)](https://starchart.cc/chentaoah/dorive)

### 🙏🏻特别感谢

- [hutool](https://gitee.com/dromara/hutool/tree/v5-master/)
- [mybatis-plus](https://gitee.com/baomidou/mybatis-plus/tree/master/)



