<h1 align="center">Dorive</h1>
<h3 align="center">轻量级领域驱动框架</h3>
<p align="center">
  <img src="https://img.shields.io/github/license/chentaoah/dorive" alt="license">
  <img src="https://img.shields.io/github/v/release/chentaoah/dorive?display_name=tag&include_prereleases" alt="release">
  <img src="https://img.shields.io/github/commit-activity/y/chentaoah/dorive" alt="commit">
  <img src="https://img.shields.io/github/stars/chentaoah/dorive?color=%231890FF&style=flat-square" alt="stars">
</p>
<hr/>

###  🎁简介

🔥🔥🔥dorive是一个轻量化渐进式开发框架，帮助开发者，快速落地领域驱动项目。 

dorive = domain + driven 或 do + driven ，是原公司项目沉淀后的开源库。

“do”表明了一种态度，只有付诸行动，才能有所收获。

### 📚概论

领域驱动设计的核心是”高内聚、低耦合“，实现方法有：微服务化、模块化、模型化。

- 微服务化：由多个较小的，松散耦合的服务组成一个应用。

- 模块化：由多个可组合、分解、更换的单元，组成一个服务。

- 模型化：对业务进行描述、抽象，形成多个对象，组成一个模块。


概念上：微服务 > 模块 > 模型

### ⚙️模块

模块应该具有以下特性：

- 能够组合、分解、更换
- 相互间通过公共服务或事件进行交互
- 能够独立运行、测试
- 对运行环境不敏感
- 提供接口，支持重写业务逻辑

### 🧊模型

模型应该具有以下特性：

- 由业务抽象而来，反映出结构、逻辑、关系
- 面向对象，支持重写业务逻辑
- 
- 能够向外传播事件

###  🍺设计理念

dorive是Mybatis-Plus的拓展，易于集成，开发者无需添加任何配置与代码。

dorive的优势：

- 极少的sql编写（与数据存储方式解耦）
- 一次建模，任意查询（代码的通用性强，开发成本低）
- 面向对象，动态拓展（可维护性、可拓展性强）
- 事件通知，代码解耦（耦合度低）
- 正向+逆向工程（开发速度快）

###  🛠️模块说明

| 模块                       | 说明             |
| -------------------------- | ---------------- |
| dorive-api                 | 领域驱动统一规范 |
| dorive-core                | 核心实现         |
| dorive-event               | 事件通知实现     |
| dorive-query               | 关联查询实现     |
| dorive-ref                 | 仓储引用实现     |
| dorive-env                 | 简易化配置       |
| dorive-sql                 | 动态查询语句实现 |
| dorive-mybatis-plus        | mybatis-plus适配 |
| dorive-inject              | 模块化依赖校验   |
| dorive-web                 | web开发适配      |
| dorive-spring-boot-starter | 启动器           |

### 🔗依赖说明

| 依赖库                    | 版本   | 说明                |
| ------------------------- | ------ | ------------------- |
| spring-boot-starter-web   | 2.7.18 | spring-boot web集成 |
| spring-tx                 | 5.3.31 | spring事务管理      |
| hutool-all                | 5.8.25 | 工具库              |
| mybatis-plus-boot-starter | 3.5.7  | 数据库操作          |

###  📦安装

因项目尚未上传至Maven中央仓库，请访问Gitee主页：[dorive](https://gitee.com/digital-engine/dorive/tree/master)，下载源码至本地后，使用Maven命令安装。

```shell
mvn install
```

然后就可以使用Maven引入了。

```xml
<dependency>
    <groupId>com.gitee.digital-engine</groupId>
    <artifactId>dorive-spring-boot-starter</artifactId>
    <version>3.5.0.6</version>
</dependency>
```

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



