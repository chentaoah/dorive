<h1 align="center">Dorive</h1>
<h3 align="center">轻量级领域驱动框架</h3>
<p align="center">
  <img src="https://img.shields.io/github/license/chentaoah/dorive" alt="license">
  <img src="https://img.shields.io/github/v/release/chentaoah/dorive?display_name=tag&include_prereleases" alt="release">
  <img src="https://img.shields.io/github/commit-activity/y/chentaoah/dorive" alt="commit">
  <img src="https://img.shields.io/github/stars/chentaoah/dorive?color=%231890FF&style=flat-square" alt="stars">
</p>
<hr/>
🔥🔥🔥轻量的领域驱动的ORM框架，帮助开发者通过建模，快速构建具有可维护性、可拓展性的应用程序。

### 👍推荐理由

- **模型驱动**
- **NoSQL**
- **面向对象**
- **事件模式**
- **代码生成**

### 🤼‍♂️设计模式对比

|          | UI驱动    | 领域驱动       |
| -------- | --------- | -------------- |
| 编码风格 | 面向过程  | ✅面向对象      |
| 上手难度 | ✅简单     | 中等           |
| 开发速度 | ✅快       | 中等           |
| 设计依据 | 原型      | ✅核心业务      |
| 交付周期 | ✅小、精确 | 一般以整体交付 |
| 技术依赖 | 数据库    | ✅无            |
| 耦合度   | 极高      | ✅低            |
| 可维护性 | 差        | ✅高            |
| 可复用性 | 差        | ✅高            |
| 可拓展性 | 差        | ✅高            |

### 🤸面临的挑战

- 业务知识的提炼与抽象
- 面向对象编程 + 数据持久化（框架发力点）

### 📊架构设计

![avatar](https://gitee.com/digital-engine/dorive/raw/master/doc/img/framework.png)

### 🏄快速开始

```xml
<dependency>
    <groupId>com.gitee.digital-engine</groupId>
    <artifactId>dorive-spring-boot-starter</artifactId>
    <version>3.4.3.3</version>
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
@QueryScan("xxx.xxx.xxx.xxx.xxx.query")
public class TenantRepository extends MybatisPlusRepository<Tenant, Integer> {
}
```

#### 定义查询对象

```java
package xxx.xxx.xxx.xxx.xxx.query;
@Data
@Example
public class TenantQuery {
    @Criterion(belongTo = "user")
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

int count = tenantRepository.insert(Selector.ALL, tenant);
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

List<Tenant> tenants = tenantRepository.selectByQuery(Selector.ALL, tenantQuery);
```

#### 更新数据

```java
Tenant tenant = tenantRepository.selectByPrimaryKey(Selector.ROOT, 1);
tenant.setTenantCode("tenant1");

int count = tenantRepository.update(Selector.ROOT, tenant);
```

#### 删除数据

```java
// 开发者通过聚合对象的id，即可删除所有数据
int count = tenantRepository.deleteByPrimaryKey(Selector.ALL, 1);
```
