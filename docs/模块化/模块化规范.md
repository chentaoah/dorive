### 项目结构

```
${project}
├── ${project}-base // 基础模块
│   └── src/main/java
│       └── ${organization}.${project}.base.${version} // 包
│           └── Application.java // 启动器
│   └── src/main/resources
│       └── META-INF
│           └── MANIFEST.MF // 模块定义
│   └── pom.xml
|
├── ${project}-${domain} // 业务模块
│   └── src/main/java
│       └── ${organization}.${project}.${domain}.${version} // 包
│           └── Application.java // 启动器
│   └── src/main/resources
│       └── META-INF
│           └── classpath.idx // 链接文件（非必需）
│           └── MANIFEST.MF // 模块定义
│   └── pom.xml
|
├── ${project}-launcher // 加载模块
│   └── src/main/java
│       └── ${organization}.${project}.launcher.${version} // 包
│           └── Application.java // 启动器
│   └── src/main/resources
│       └── META-INF
│           └── MANIFEST.MF // 模块定义
│   └── pom.xml
|
└── pom.xml
```

- ${organization}：组织名（例如com.gitee）
- ${project}：项目名
- ${domain}：领域名
- ${version}：版本（一般为v1）

### 依赖关系

加载模块 * 1 ==> 业务模块 * N ==> 基础模块 * 1

- 业务模块之间不建议直接依赖
- 业务模块之间通过接口调用（接口放置在基础模块中）

### 模块结构

```
${organization}.${project}.${domain}.${version} // 包
├── api // 展现层
|	└── controller // 控制器
|	└── dto // 传输对象
│	└── dto2 // 复杂传输对象
│	└── vo // 视图对象
│	└── vo2 // 复杂视图对象
|
├── app // 应用层
│	└── service // 服务
|
├── domain // 领域层
│	└── entity // 实体对象
│	└── query // 查询对象
│	└── repository // 仓储
|
├── infra // 基础设施层
│	└── mapper // 数据操作接口
│	└── pojo // 持久化对象
|
└── Application.java // 启动器
```

- 未应用领域驱动设计的项目，可移除领域层。

### 启动器

Application.java：

```java
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

- 让每个模块，都具备独立运行、测试的能力。

### 模块定义

MANIFEST.MF：

```properties
# 来源（标记源代码的出处）
X-Module-Origin: null
# 【组织】
X-Module-Organization: ${organization}
# 【项目】
X-Module-Project: ${project}
# 【领域】
X-Module-Domain: ${domain}
# 子域
X-Module-Subdomain: null
# 【名称】（与Maven模块名称相同）
X-Module-Name: ${project}-${domain}
# 【版本】（一般为v1）
X-Module-Version: ${version}
# 描述
X-Module-Description: null
# 【类型】（base-基础模块、biz-业务模块、launcher-加载模块、single-单体模块）
X-Module-Type: ${type}
# 标签
X-Module-Tags: null
# 激活配置（等于spring boot profile）
X-Module-Profiles: null
# 独占配置（仅当前模块可访问，一旦指定，则无法引入全局配置）
X-Module-Configs: application-${domain}.yml
# 引入全局配置的包或类（优先级高于独占配置）
X-Module-Global-Values: ${organization}.${project}.${domain}.${version}.**
# 暴露的包或类（语法参考AntPath）
X-Module-Exports: ${organization}.${project}.${domain}.${version}.**
# 依赖资源限定名
X-Module-Requires: xxxx.xxxx.xxxx.xxxx.AService
# 提供资源限定名
X-Module-Provides: xxxx.xxxx.xxxx.xxxx.BService
# 通知对象限定名
X-Module-Notifies: xxxx.xxxx.xxxx.xxxx.AListener
# 等待对象限定名
X-Module-Waits: xxxx.xxxx.xxxx.xxxx.BListener
# 表名前缀（生成代码时使用）
X-Module-Table-Prefix: ${domain}
# 请求前缀（默认为${名称}/${版本}）
X-Module-Request-Prefix: null
```

- 【】代表必输

### 链接文件

 classpath.idx：

```properties
# 加载模块
module: ${project}/${module}/target/classes
# 加载jar
maven: ${groupId}:${artifactId}:${version}
```

- 运行时，动态加载依赖的模块与代码库。
- 一般用于测试场景。