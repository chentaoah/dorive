### 项目结构

```
${project}
├── ${project}-base // 基座模块
│   └── src/main/java
│       └── ${organization}.${project}.base.${version} // 包
│           └── Application.java // 启动器
│   └── src/main/resources
│       └── META-INF
│           └── MANIFEST.MF // 模块定义
│   └── pom.xml
├── ${project}-${domain} // 业务模块
│   └── src/main/java
│       └── ${organization}.${project}.${domain}.${version} // 包
│           └── Application.java // 启动器
│   └── src/main/resources
│       └── META-INF
│           └── MANIFEST.MF // 模块定义
│   └── pom.xml
├── ${project}-launcher // 加载模块
│   └── src/main/java
│       └── ${organization}.${project}.launcher.${version} // 包
│           └── Application.java // 启动器
│   └── src/main/resources
│       └── META-INF
│           └── MANIFEST.MF // 模块定义
│   └── pom.xml
└── pom.xml
```

- ${organization}：组织名（例如com.gitee）
- ${project}：项目名
- ${domain}：业务域名
- ${version}：版本（一般为v1）

### 依赖关系

1 * ${project}-launcher ==> N * ${project}-${domain} ==> 1 * ${project}-base

### 模块结构

```
${organization}.${project}.${domain}.${version} // 包
├── api // 展现层
|	└── controller // 控制器
|	└── dto // 传输对象
│	└── dto2 // 复杂传输对象
│	└── vo // 视图对象
│	└── vo2 // 复杂视图对象
├── app // 应用层
│	└── service // 服务
├── domain // 领域层
│	└── entity // 实体对象
│	└── query // 查询对象
│	└── repository // 仓储
├── infra // 基础设施层
│	└── mapper // 数据操作接口
│	└── pojo // 持久化对象
└── Application.java // 启动器
```

- 项目未使用领域驱动，可移除领域层。

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

- 保证每一个模块，都具备独立运行、测试的能力。

### 模块定义

MANIFEST.MF：

```properties
# 来源（标记源代码的出处，一般为null）
X-Module-Origin: null
# 组织
X-Module-Organization: ${organization}
# 项目
X-Module-Project: ${project}
# 领域
X-Module-Domain: ${domain}
# 子域（一般为null）
X-Module-Subdomain: null
# 模块（与Maven模块名称相同）
X-Module-Name: ${project}-${domain}
# 版本（一般为v1）
X-Module-Version: ${version}
# 描述（一般为null）
X-Module-Description: ${description}
# 类型（base-基础模块、biz-业务模块、launcher-加载模块、single-单体模块）
X-Module-Type: ${type}
# 标签（一般为null）
X-Module-Tags: null
# 激活配置（等效于spring boot profile）
X-Module-Profiles: null
# 独享配置
X-Module-Configs: application-${domain}.yml
# 对外暴露的包或类（语法参考AntPath）
X-Module-Exports: ${organization}.${project}.${domain}.${version}.**
# 依赖资源（限定名）
X-Module-Requires: xxxx.xxxx.xxxx.AService
# 提供资源（限定名）
X-Module-Provides: xxxx.xxxx.xxxx.BService
# 通知对象（限定名）
X-Module-Notifies: xxxx.xxxx.xxxx.CListener
# 等待对象（限定名）
X-Module-Waits: xxxx.xxxx.xxxx.DListener
# 表名前缀（生成代码时使用）
X-Module-Table-Prefix: ${domain}
# 请求前缀（默认为${X-Module-Name}/${X-Module-Version}）
X-Module-Request-Prefix: null
```

