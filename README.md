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



### ✨痛点

#### **1. 开发与演进之痛**

- **“牵一发而动全身”**：想修改一个简单功能或添加一个字段，却引发一连串意想不到的报错，需要修改几十个文件，测试范围无法估量，风险极高。
- **“新需求无处安放”**：面对新的业务需求，发现现有架构根本无法优雅支持。代码里到处是“如果……就……”的条件分支，最后只能打上又一个丑陋的补丁，系统变得越发臃肿。
- **无人敢动祖传代码”**：核心模块由早已离职的“大神”编写，无人完全理解，且没有测试覆盖。所有人都在祈祷它不要出问题，更别提优化和重构。

#### **2. 维护与交付之痛**

- **“发布时间远超开发时间”**：一个简单的功能，开发只需2天，但集成、测试、修复因耦合带来的副作用却需要2周。发布周期漫长，业务响应速度迟缓。
- **“修复一个Bug，引入两个新Bug”**：由于模块间存在隐式耦合和全局状态，缺陷修复如同在满是地雷的战场上排雷，解决问题的同时常常创造更多问题。
- **“技术债利滚利，压垮团队”**：每次迭代为了赶工期，都选择最简单的“硬编码”方案。技术债不断累积，最终团队大部分精力都用于偿还旧债，而非创造新价值，士气低落。

#### **3. 协作与扩展之痛**

- **“团队互相阻塞，效率低下”**：因为代码强耦合，前端、后端、不同业务小组的工作相互依赖，无法并行开发。一个人未完成的模块会阻塞整个团队。
- **“想用新技术？痴人说梦！”**：系统被陈旧的框架和技术栈牢牢锁定，想要引入一个现代化的工具库或升级版本，成本高到无法接受，只能“苟延残喘”。
- **“无法按需扩展”**：系统是一个“巨石怪兽”，即使只有某个业务模块访问量激增，也不得不扩展整个应用，造成资源浪费和成本飙升。

#### **4. 业务与生存之痛**

- **“市场机会从指缝中溜走”**：竞争对手可以每周上线新功能，而你却需要数月。业务人员眼睁睁看着市场机会流失，对技术团队失去信心。
- **“创新实验成本极高”**：想要尝试一个微小的业务创新或A/B测试，需要在混乱的代码中“披荆斩棘”，导致创新想法在技术评估阶段就被扼杀。
- **“系统成为业务发展的最大瓶颈”**：业务战略需要快速调整或开辟新战线，但技术系统僵硬无比，无法提供支撑，从“业务赋能者”沦为了“业务绊脚石”。



### 🏗️3M架构

- **模块化 (Module)**：应用由可独立运行与测试的模块构成
- **模型化 (Model)**：模块由业务抽象而来的领域模型构成
- **微服务化(Microservices)**： 天然适配微服务架构



### 💡相关资料

- 逻辑架构图：[logic.png](https://gitee.com/digital-engine/dorive/blob/3.5.5/doc/img/logic.png)
- 物理架构图：[physical.png](https://gitee.com/digital-engine/dorive/blob/3.5.5/doc/img/physical.png)
- 项目文档：[Gitee wiki](https://gitee.com/digital-engine/dorive/wikis/pages)
- 测试案例：[dorive-example](https://gitee.com/digital-engine/dorive-example)



### 🚅快速开始

```xml
<dependency>
    <groupId>com.gitee.digital-engine</groupId>
    <artifactId>dorive-launcher</artifactId>
    <version>3.5.5</version>
</dependency>
```



### 🤝依赖项

| 依赖库                    | 版本    | 说明            |
| ------------------------- | ------- | --------------- |
| spring-boot-starter-web   | 2.7.18  | spring boot web |
| spring-boot-starter-aop   | 2.7.18  | spring boot aop |
| spring-tx                 | 5.3.31  | spring事务管理  |
| lombok                    | 1.18.16 | 样板代码生成    |
| commons-lang3             | 3.9     | 工具库          |
| hutool-all                | 5.8.25  | 工具库          |
| mybatis-plus-boot-starter | 3.5.7   | 数据库框架      |



### 🐞bug反馈与建议

提交问题反馈请说明正在使用的JDK版本、dorive版本，以及依赖库版本。

页面地址：[Gitee issue](https://gitee.com/digital-engine/dorive/issues)



### 🙏🏻特别感谢

- [hutool](https://gitee.com/dromara/hutool/tree/v5-master/)
- [mybatis-plus](https://gitee.com/baomidou/mybatis-plus/tree/master/)



