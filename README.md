## Spring Domain

轻量级领域驱动框架。

- **支持实体和数据库表联动，同步增、删、改。**
- **支持通过简单对象查询实体，而无需考虑字段所属表结构。**
- **支持实体继承，并根据类型进行动态级联查询。**

## 什么是领域驱动？

&emsp;&emsp;在讨论什么是领域驱动之前，让我们思考一下什么是需求驱动。

&emsp;&emsp;需求驱动，从字面来理解，就是产品经理提需求，开发工程师按照需求，开展设计与编码工作。开发工程师一般先设计表结构，再通过工具反向生成映射对象。映射对象只包含字段，没有业务方法，业务逻辑都放在Service层。这是典型的”贫血模式“。

&emsp;&emsp;这样做的好处是，基本不需要复杂的设计，开发流程简单，可以做到快速开发和上线。

&emsp;&emsp;这样做的缺点是，设计、编码和产品需求耦合度高，缺乏可拓展性、可维护性。
