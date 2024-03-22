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

​		dorive是一个轻量的以领域驱动为核心的业务框架，它提供了诸多开箱即用的功能，旨在帮助开发者，在项目中快速、便捷地应用领域驱动。

​		这些功能涵盖了依赖注入校验、依赖即用配置、实体定义与映射、级联查询与操作、实体多态、实体事件通知、复杂推导查询、ref关键字、复杂计数统计、表结构生成、数据库逆向生成、接口代码生成等，可以满足大部分开发场景。

###  🎁名称由来

​		dorive = domain + driven 或 do + driven ，是原公司项目沉淀后的开源库。“do”表明了一种态度，只有付诸行动，才能有所收获。

###  🍺设计理念

​		dorive是Mybatis-Plus的拓展，易于集成，开发者无需添加任何配置与代码。

​		dorive的优势：

- 极少的sql编写（与数据存储方式解耦）
- 一次建模，任意查询（代码的通用性强，开发成本低）
- 面向对象，动态拓展（可维护性、可拓展性强）
- 事件通知，代码解耦（耦合度低）
- 正向+逆向工程（开发速度快）

### 📊架构设计

<img src="https://gitee.com/digital-engine/dorive/raw/master/doc/img/framework.png" alt="avatar" style="zoom: 40%;" />

###  🛠️模块说明

| 模块                       | 说明                                                 |
| -------------------------- | ---------------------------------------------------- |
| dorive-inject              | 实现了依赖注入校验                                   |
| dorive-env                 | 实现了依赖即用配置                                   |
| dorive-web                 | 提供了web开发时会用到的工具类                        |
| dorive-proxy               | 动态代理工具包                                       |
| dorive-api                 | 内含实体定义规范                                     |
| dorive-core                | 核心实现（实体定义与映射、级联查询与操作、实体多态） |
| dorive-event               | 实现了实体事件通知                                   |
| dorive-query               | 实现了复杂推导查询                                   |
| dorive-ref                 | 实现了ref关键字                                      |
| dorive-sql                 | 实现了复杂计数统计                                   |
| dorive-mybatis-plus        | 提供mybatis-plus底层实现                             |
| dorive-spring-boot-starter | 依赖管理启动器                                       |

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
    <version>3.4.3.4</version>
</dependency>
```

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

### 🔗依赖说明

| 依赖库                    | 版本            | 说明                |
| ------------------------- | --------------- | ------------------- |
| spring-boot-starter-web   | 2.7.8（可降级） | spring-boot web集成 |
| spring-tx                 | 5.3.9           | spring事务管理      |
| hutool-all                | 5.8.12          | 工具库              |
| javassist                 | 3.29.2-GA       | 动态代理库          |
| mybatis-plus-boot-starter | 3.5.3.1         | 数据库操作          |

### 📝文档

wiki地址：

- [Gitee wiki](https://gitee.com/digital-engine/dorive/wikis/pages)

### 🐞bug反馈与建议

提交问题反馈请说明正在使用的JDK版本、dorive版本，以及依赖库版本。

- [Gitee issue](https://gitee.com/digital-engine/dorive/issues)

### 🧑‍🤝‍🧑与我（们）一起

1. 请将个人联系方式，发送邮件至digital_engine@163.com
2. 等待维护者向你发出邀请

###  ⭐Star dorive

[![Stargazers over time](https://starchart.cc/chentaoah/dorive.svg?variant=adaptive)](https://starchart.cc/chentaoah/dorive)

### 🙏🏻特别感谢

- [hutool](https://gitee.com/dromara/hutool/tree/v5-master/)
- [mybatis-plus](https://gitee.com/baomidou/mybatis-plus/tree/master/)



