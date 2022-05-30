## Spring Domain

Spring体系下实现的领域驱动框架。

## 依赖管理

### 示意图

![avatar](https://gitee.com/digital-engine/spring-domain/raw/master/static/img/layer.png)

领域驱动的分层方式，有别于传统的三层。分别为：表现层、应用层、领域层、基础设施层。

依赖管理包括两方面：

- 跨层级，不建议直接调用。
- 相同层级间，不建议直接调用内部服务。

### 配置

```yaml
spring:
  domain:
    enable: true
    scan: com.company.project.**        # 扫描包名
    domains:
      - name: dal                       # 一个名叫dal的领域
        pattern: com.company.project.dal.**
        protect: com.company.project.dal.mapper.** # 该领域内不建议外部直接调用的服务
      - name: dal-domain                # 一个名叫dal-domain的领域，它是dal的子域
        pattern: com.company.project.domain.**
      - name: serviceA                  # 一个名叫serviceA的领域
        pattern: com.company.project.service.a.**
      - name: serviceB                  # 一个名叫serviceB的领域
        pattern: com.company.project.service.b.**
        protect: com.company.project.service.b.internal.** # 该领域内不建议外部直接调用的服务
```

通过配置，定义了多个领域。分别为：dal、dal-domain、serviceA、serviceB

![avatar](https://gitee.com/digital-engine/spring-domain/raw/master/static/img/divide.png)

## 实体映射

### 示意图

![avatar](https://gitee.com/digital-engine/spring-domain/raw/master/static/img/entity.png)

名词解释：

- School（学校）
- Grade（年级）

一个学校可以包含多个年级，一个年级可以包含多个班级，一个班级又可以包含多个学生。

School实体对象的数据来源于database_school表，但字段无需完全一致。

Grade实体对象的数据来源于database_grade表，但字段无需完全一致。

### 创建模型

```java
@Data
@Entity(mapper = DatabaseSchoolMapper.class)
public class School {
    private Integer id;
    private String name;
    
    @Entity(mapper = DatabaseGradeMapper.class)
    @Binding(field = "schoolId", bind = "./id")
    private List<Grade> grades;
}
```

```java
@Data
public class Grade {
    private Integer id;
    private Integer schoolId;
    private String name;
	private List<Class> classes;
}
```

### 创建仓储

```java
@RootRepository
public class SchoolRepository extends MybatisPlusRepository<School, Integer> {
}
```

### 调用仓储

```java
@Service
@AllArgsConstructor
public class SchoolServiceImpl implements SchoolService {

    private final SchoolRepository schoolRepository;

    @Override
    public School querySchoolById(Integer id) {
		return schoolRepository.selectByPrimaryKey(id);
    }
}
```

执行逻辑：

- DatabaseSchoolMapper根据id，查询出DatabaseSchool实例，并映射到School实体上。
- DatabaseGradeMapper再根据School实体的id，查询出DatabaseGrade实例，并映射到Grade实体上。