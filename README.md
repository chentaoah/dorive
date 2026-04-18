<h1 align="center">Dorive</h1>
<h3 align="center">轻量化、模块化、渐进式领域驱动开发框架</h3>
<p align="center">
  <img src="https://img.shields.io/github/license/chentaoah/dorive" alt="license">
  <img src="https://img.shields.io/github/v/release/chentaoah/dorive?display_name=tag&include_prereleases" alt="release">
  <img src="https://img.shields.io/github/commit-activity/y/chentaoah/dorive" alt="commit">
</p>
<hr/>
### 🎯项目概述

🔥🔥🔥dorive是一个**轻量化、模块化、渐进式领域驱动开发框架**，帮助开发者，开发**可持续演进的复杂应用**。 

dorive提供了**模块化、模型化**的解决方案，以应对复杂应用的**僵化、腐化**问题。



### ✨为什么僵化、腐化？

- **未建立统一语言**： 产品经理、开发人员、测试人员对业务概念、规则的认知存在差异。
- **未划分业务边界**：模块的定位、职责不清晰，代码组织混乱。
- **未抽象业务模型**：实现业务过程，却未沉淀业务资产。
- **重视敏捷，轻视治理**：赶工期，大量采用硬编码与临时方案，形成了难阅读、难修改的历史代码。忽视文档输出、代码审查等流程，导致代码逻辑隐晦，质量下降，隐性技术负债不断滚雪球。



###  🏗️解决方案

- **需求结构化、业务建模**：统一认知差异。

- **模块化 **：划分业务边界。
- **模型化**：抽象业务模型。
- **模块、模型治理**：全生命周期管理。



### 📖参考资料

- 逻辑架构图：[logic.png](https://gitee.com/digital-engine/dorive/blob/master/doc/png/logic.png)
- 物理架构图：[physical.png](https://gitee.com/digital-engine/dorive/blob/master/doc/png/physical.png)
- 项目文档：[Gitee wiki](https://gitee.com/digital-engine/dorive/wikis/pages)
- 测试案例：[dorive-example](https://gitee.com/digital-engine/dorive-example)



### 🚅快速开始

```xml
<dependency>
    <groupId>com.gitee.digital-engine</groupId>
    <artifactId>dorive-launcher</artifactId>
    <version>4.0.0</version>
</dependency>
```



### 🤝依赖项

| 依赖库                            | 版本    | 说明            |
| --------------------------------- | ------- | --------------- |
| jdk                               | 17      | Java 开发工具包 |
| spring-boot-starter-web           | 3.5.13  | spring boot web |
| spring-boot-starter-aop           | 3.5.13  | spring boot aop |
| spring-tx                         | 6.2.17  | spring事务管理  |
| lombok                            | 1.18.44 | 样板代码生成    |
| commons-lang3                     | 3.17.0  | 工具库          |
| hutool-all                        | 5.8.43  | 工具库          |
| mybatis-plus-spring-boot3-starter | 3.5.13  | 数据库框架      |



### 🐞bug反馈与建议

提交问题反馈请说明正在使用的JDK版本、dorive版本，以及依赖库版本。

页面地址：[Gitee issue](https://gitee.com/digital-engine/dorive/issues)



### 🙏🏻特别感谢

- [hutool](https://gitee.com/dromara/hutool/tree/v5-master/)
- [mybatis-plus](https://gitee.com/baomidou/mybatis-plus/tree/master/)



