<h1 align="center">Dorive</h1>
<h3 align="center">轻量化、渐进式的微应用开发框架</h3>
<p align="center">
  <img src="https://img.shields.io/github/license/chentaoah/dorive" alt="license">
  <img src="https://img.shields.io/github/v/release/chentaoah/dorive?display_name=tag&include_prereleases" alt="release">
  <img src="https://img.shields.io/github/commit-activity/y/chentaoah/dorive" alt="commit">
</p>
<hr/>

### 🎯项目概述

**Dorive** 是一个**轻量化、渐进式**的微应用开发框架，专为构建**可持续演进的复杂应用**而设计。它通过提供**模块化**与**模型化**的系统性解决方案，帮助开发团队有效应对复杂业务系统在长期迭代中普遍面临的**僵化**与**腐化**问题，让软件架构随业务发展保持清晰、灵活与可维护。

### ✨特性

**轻量化设计**

- 仅依赖 Spring Boot、MyBatis-Plus 等少量基础组件，无重度框架绑定
- 核心包体积小，启动快，学习成本低
- 按需引入，不强制全量使用，可逐步接入现有项目

**开发体验友好**

- 基于 Spring Boot 生态，与主流技术栈无缝集成
- 提供启动器（dorive-launcher），开箱即用
- 配套示例项目与文档，快速上手

**渐进式落地**

- 支持从简单 CRUD 到复杂领域模型的平滑过渡
- 不要求一次性完成领域建模，可随业务理解深入逐步演进
- 兼容传统分层架构，降低团队转型门槛

**实现微应用架构**

- 以业务边界为核心进行模块划分，职责清晰、边界明确
- 模块间低耦合、高内聚，支持独立开发、测试与部署
- 模块可插拔，便于替换、扩展与重组，适应业务变化

**实现整洁架构**

- 遵循依赖倒置原则，领域层不依赖基础设施与框架细节
- 业务逻辑独立于数据库、Web 框架、第三方服务等外部实现
- 分层清晰、边界明确，核心业务可独立测试与演进
- 外部实现可替换，降低技术选型对业务代码的侵入与绑定

**领域模型驱动**

- 提供实体、值对象、聚合、领域服务、领域事件等建模能力
- 业务规则内聚于模型，避免逻辑散落与贫血模型
- 沉淀业务资产，让代码成为可读、可演进的业务文档

**统一语言落地**

- 通过结构化需求与业务建模，统一产品、开发、测试的认知
- 领域术语直接映射为代码结构，减少沟通歧义
- 模型即文档，降低知识传递与交接成本

**可持续演进**

- 架构随业务成长而成长，避免推倒重来
- 模块与模型可独立演进，降低变更影响范围
- 支持长期维护，让复杂系统保持可理解、可修改

### 📖参考资料

- 项目文档：[dorive-docs](https://gitee.com/digital-engine/dorive-docs)

- 测试案例：[dorive-example](https://gitee.com/digital-engine/dorive-example)

### 🐞bug反馈与建议

提交问题反馈请说明正在使用的JDK版本、dorive版本，以及依赖库版本。

页面地址：[Gitee issue](https://gitee.com/digital-engine/dorive/issues)

