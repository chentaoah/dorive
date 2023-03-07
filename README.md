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

**推荐理由：**

- **级联新增、查询、更新、删除**
- **NoSQL**
- **面向对象**
- **事件驱动**
- **代码生成**

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

- 业务变化快，应用程序快速无序膨胀，直到无法支撑业务而重构。
- 需求分析不足，满足于原型，容易偏离业务核心。
- 下层的改动，容易在上层被放大，导致开发成本高，系统内部僵化。
- 应用程序开发，无法脱离数据库。
- 难以使用统一有效的方式，构建复杂的应用程序。

### 👬领域驱动

**以业务核心驱动模型设计，再以模型设计驱动代码开发的方式，称为领域驱动。**

**优点：**

- 领域模型可以作为产品、开发、测试人员沟通时的统一语言。
- 领域模型数据高度内聚，可维护性强、代码耦合低。
- 领域模型的开发过程是面向对象编程，可复用性、可拓展性强。
- 能最大程度，减少系统内部的僵化与系统间的腐化。
- 不强依赖于数据库，容易与不同的存储引擎进行集成。
- 在战略层面，能借用领域模型，进行业务架构的顶层设计。

**缺点：**

- 对开发人员的要求高。（能够掌握一些常见的设计模式）
- 相较于UI驱动，需要做前期的准备工作。（沟通与消化业务知识）
- 一般以整个模块交付，不容易以较小的单元交付。

### 🤸面临的挑战

- 业务知识的提炼与抽象
- 面向对象编程 + 数据持久化

### 📊架构设计

![avatar](https://gitee.com/digital-engine/dorive/raw/master/doc/img/framework.png)

### 🏄快速开始

```xml
<dependency>
    <groupId>com.gitee.digital-engine</groupId>
    <artifactId>dorive-spring-boot-starter</artifactId>
    <version>3.3.1</version>
</dependency>
```

### 🧡代码示例

以Saas化场景为例，为租户建模。

#### 定义实体

```java
/**
 * 租户聚合
 * name 实体名称
 * source 数据来源
 */
@Data
@Entity(name = "tenant", source = SysTenantMapper.class)
public class Tenant {
    /**
     * 选取器，决定每次操作的范围
     */
    public static final Selector ALL = new NameSelector("*");
    public static final Selector ONLY_TENANT = new NameSelector("tenant");
    
    private Integer id;
    private String tenantCode;
    
    /**
     * 部门实体
     * field 字段名称
     * bindExp 绑定字段表达式
     */
    @Entity
    @Binding(field = "tenantId", bindExp = "./id")
    private List<Department> departments;
    
    /**
     * 用户实体
     * property 绑定对象内部属性
     */
    @Entity
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
    private String sortBy;
    private String order;
    private Integer page;
    private Integer limit;
}
```

#### 新增数据

```java
// 开发者无需设置实体之间的关联id
Tenant tenant = new Tenant();
tenant.setTenantCode("tenant");

Department department = new Department();
department.setDeptCode("dept");
tenant.setDepartments(Collections.singletonList(department));

User user = new User();
user.setUserCode("user");
tenant.setUser(Collections.singletonList(user));

int count = tenantRepository.insert(Tenant.ALL, tenant);
```

#### 查询数据

```java
// 开发者无需编写复杂的查询SQL
TenantQuery tenantQuery = new TenantQuery();
tenantQuery.setUserCode("000001");
tenantQuery.setSortBy("id");
tenantQuery.setOrder("desc");
tenantQuery.setPage(1);
tenantQuery.setLimit(10);

List<Tenant> tenants = tenantRepository.selectByCoating(Tenant.ALL, tenantQuery);
```

#### 更新数据

```java
Tenant tenant = tenantRepository.selectByPrimaryKey(Tenant.ONLY_TENANT, 1);
tenant.setTenantCode("tenant1");

int count = tenantRepository.update(Tenant.ONLY_TENANT, tenant);
```

#### 删除数据

```java
// 开发者通过聚合对象的id，即可删除所有数据
int count = tenantRepository.deleteByPrimaryKey(Tenant.ALL, 1);
```
