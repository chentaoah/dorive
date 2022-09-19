## Spring Domain

&emsp;&emsp;轻量级领域驱动框架。

- **支持实体和数据库表联动，同步增、删、改。**
- **支持通过简单对象查询实体，而无需考虑字段所属表结构。**
- **支持实体继承，并根据类型进行动态级联查询。**

## 什么是领域驱动？

&emsp;&emsp;在讨论什么是领域驱动之前，让我们思考一下什么是需求驱动。

## 需求驱动

&emsp;&emsp;从字面来理解，就是产品经理提需求，开发工程师按照需求，开展设计与编码工作。开发工程师一般先设计表结构，再通过逆向工程生成映射对象。映射对象只包含业务字段，不包含业务方法，业务逻辑都统一放在Service层。这是典型的”失血模式“。

&emsp;&emsp;需求驱动的特点：

- 基本不需要复杂的设计，开发简单，可以做到快速开发和上线。
- 设计、编码与需求耦合度高，无法应对更复杂的业务场景。


## 领域驱动

&emsp;&emsp;同样是产品经理提需求，开发工程师按照需求，开展设计与编码工作。不过，在设计之前，开发工程师需要思考以下几个问题：

- 需求所属的业务领域是什么？
- 该业务领域是否存在通用的数据模型？
- 该数据模型是否存在具体的业务行为？
- 该业务领域是否存在细分的子业务领域？
- 该子业务领域中，数据模型的业务行为是否会发生变化？ 

&emsp;&emsp;从以上几个问题，隐约能够嗅到“面向对象编程”的味道。在领域驱动设计理念中，上述数据模型被称为“领域模型”。

&emsp;&emsp;开发工程师一般先设计实体类（Java对象），再配置实体类和数据库表的映射关系。实体类包含业务字段，也包含业务方法，业务方法可以被子类重写。这是典型的“充血模式”。

&emsp;&emsp;领域模型的优势：

- 与现实世界中的概念相似，容易被产品经理、前端工程师、后端工程师、测试工程师所理解。

- 允许多层嵌套，数据结构高度内聚。

- 具有面向对象编程的特性，支持在不同的业务领域，发生不同的业务行为。

  领域模型的劣势：

- 要求开发工程师对业务十分熟悉，最好是领域专家。
- 要求开发工程师理解面向对象编程，并掌握常见的设计模式。