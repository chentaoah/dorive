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

### 🏋SmartUI模式

**以UI界面驱动数据库设计，再以数据库设计驱动代码开发的模式，称为“SmartUI模式”。**

**优点：**

- 效率高，能在短时间内实现简单的应用程序。
- 对开发人员的要求低，几乎不需要培训。
- 有时可以克服需求分析上的不足，只需要满足原型即可。
- 程序之间独立性高，可以相对准确地安排小的交付周期。
- 能够便捷地在关系数据库上构建应用。
- 通过UI界面，能够大致了解后台的逻辑。

**缺点：**

- 只满足原型，但无法满足快速变化的新的业务场景。
- 缺乏对业务行为的提炼，容易偏离业务核心，二次开发成本高。
- 应用程序开发，无法脱离数据库。
- 难以使用统一有效的方式，构建复杂的应用程序。

### 👬领域驱动

**以业务核心驱动模型设计，再以模型设计驱动代码开发的方式，称为领域驱动。**

**优点：**

- 领域模型是业务知识的高度概括，容易在产品、开发、测试人员之间进行传播。
- 领域模型数据高度内聚，可维护性强、代码耦合低。
- 领域模型的开发过程是面向对象编程，可复用性、可拓展性强。
- 不强依赖于数据库，容易与不同的存储引擎进行集成。
- 在战略层面，能借用领域模型，进行业务架构的顶层设计。
- 能最大程度，减少系统内部的僵化与系统间的腐化。

**缺点：**

- 对开发人员的要求高。（能够理解和运用一些设计模式）
- 相较于UI驱动，需要做前期的准备工作。（沟通与消化业务知识）
- 一般以整个模块交付，不容易以较小的单元交付。

### 🤸面临的挑战

- 业务知识的提炼和抽象
- 面向对象编程 + 数据持久化

### 🕺功能点

| 功能点         | 描述                                                         |
| -------------- | ------------------------------------------------------------ |
| 依赖注入校验   | 依赖注入时，校验Bean所属的领域是否一致。如果不一致，会给出警告。 |
| 模型声明       | 启动时，将带有@Entity注解的Java类，解析为一个实体。多个实体嵌套，视为一个聚合。<br />通过仓储，可以对聚合，进行级联增、删、改、查操作。 |
| 事件通知       | 当仓储操作一个聚合时，会向外发送增、删、改事件。             |
| 实体动态构造   | 通过自定义工厂，可以实现动态构造实体对象。从而，实现方法重写、以及动态级联查询。 |
| 实体变更持久化 | 实体变更自身属性后，间接同步到数据库。                       |
| 表结构生成     | 根据实体，生成对应的建表语句。                               |

### 📖项目结构

| 模块                       | 描述                                          |
| -------------------------- | --------------------------------------------- |
| dorive-injection           | 实现依赖注入校验                              |
| dorive-proxy               | 实现动态代理，取代反射                        |
| dorive-core                | 实现实体解析、仓储CRUD的核心逻辑              |
| dorive-event               | 实现仓储操作时的事件通知机制                  |
| dorive-coating             | 实现通过防腐层对象的查询机制                  |
| dorive-spring-boot-starter | 实现与mybatis-plus的集成                      |
| dorive-generator           | 实现根据实体生成数据库表结构                  |
| dorive-service             | 实现抽象的Service与Controller，供开发者继承。 |

### 📊架构设计

![avatar](https://gitee.com/digital-engine/dorive/raw/master/doc/img/domain_model.png)

### 🏄快速开始

```xml
<dependency>
    <groupId>com.gitee.digital-engine</groupId>
    <artifactId>dorive-spring-boot-starter</artifactId>
    <version>3.0.0</version>
</dependency>
```

### 🧡代码示例

以Saas化场景为例，为租户建模。

#### 定义实体

```java
/**
 * 租户实体，此处亦可视为租户聚合
 * mapper 数据源
 */
@Data
@Entity(mapper = SysTenantMapper.class)
public class Tenant {
    /**
     * 场景值集合。
     * 一般来说，在定义实体的同时，会定义触发的场景值。开发者可以指定场景值，来决定每次操作的范围。
     * 定义常量ALL的作用是，将常用的场景值集合统一维护起来。
     */
    public static final String[] ALL = new String[]{"depts", "users"};
    
    private Integer id;
    private String tenantCode;
    
    /**
     * 部门实体
     * scenes 场景值
     * mapper 数据源
     * field 实体字段
     * bindExp 绑定字段表达式
     */
    @Entity(scenes = "depts", mapper = SysDeptMapper.class)
    @Binding(field = "tenantId", bindExp = "./id")
    private List<Department> departments;
    
    /**
     * 用户实体
     * property 绑定字段的内部属性
     */
    @Entity(scenes = "users", mapper = SysUserMapper.class)
    @Binding(field = "deptId", bindExp = "./departments", property = "id")
    private List<User> users;
}
```

#### 定义仓储

```java
@RootRepository
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@CoatingScan("xxx.xxx.xxx.xxx.xxx.query")
public class TenantRepository extends MybatisPlusRepository<Tenant, Integer> {
}
```

#### 定义查询对象

```java
package xxx.xxx.xxx.xxx.xxx.query;
@Data
@Coating
public class TenantQuery {
    private String userCode;
    private String orderByDesc;
    private Integer pageNum;
    private Integer pageSize;
}
```

#### 新增数据

```java
BoundedContext boundedContext = new BoundedContext(Tenant.ALL);

// 开发者无需设置实体之间的关联id
Tenant tenant = new Tenant();
tenant.setTenantCode("tenant");

Department department = new Department();
department.setDeptCode("dept");
tenant.setDepartments(Collections.singletonList(department));

User user = new User();
user.setUserCode("user");
tenant.setUser(Collections.singletonList(user));

int count = tenantRepository.insert(boundedContext, tenant);
```

#### 查询数据

```java
BoundedContext boundedContext = new BoundedContext(Tenant.ALL);

// 开发者无需编写复杂的查询SQL
TenantQuery tenantQuery = new TenantQuery();
tenantQuery.setUserCode("000001");
tenantQuery.setOrderByDesc("id");
tenantQuery.setPageNum(1L);
tenantQuery.setPageSize(10L);

List<Tenant> tenants = tenantRepository.selectByCoating(boundedContext, tenantQuery);
```

#### 更新数据

```java
BoundedContext boundedContext = new BoundedContext();

Tenant tenant = tenantRepository.selectByPrimaryKey(boundedContext, 1);
tenant.setTenantCode("tenant1");

int count = tenantRepository.update(boundedContext, tenant);
```

#### 删除数据

```java
BoundedContext boundedContext = new BoundedContext(Tenant.ALL);
// 开发者通过聚合对象的id，即可删除所有数据
int count = tenantRepository.deleteByPrimaryKey(boundedContext, 1);
```

#### 