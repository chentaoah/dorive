## Spring Domain

框架的目的：只一行代码实现：

- **多表联动新增、删除、更新、查询。**
- **直接得到实体对象。**
- **使用DTO查询，表结构改变，代码不用改。**

## 快速开始

![avatar](https://gitee.com/digital-engine/spring-domain/raw/master/static/img/model.png)

### 创建实体

```java
@Data
@Entity(mapper = SysDbSchoolMapper.class)
public class School {
    private Integer id;
    private String schoolName;
    
    @Entity(mapper = SysDbTeacherMapper.class)
    @Binding(field = "schoolId", bind = "./id")
    private List<Teacher> teachers;
    
    @Entity(repository = ClassRepository.class) // 多层嵌套
    @Binding(field = "schoolId", bind = "./id")
    private List<Class> classes;
}
```

```java
@Data
public class Teacher {
    private Integer id;
    private Integer schoolId;
    private String teacherName;
	private boolean model; // 是否校内模范
}
```

```java
@Data
@Entity(mapper = SysDbClassMapper.class)
public class Class {
    private Integer id;
    private Integer schoolId;
    private String className;
    
    @Entity(mapper = SysDbStudentMapper.class)
    @Binding(field = "classId", bind = "./id")
	private List<Student> students;
}
```

```java
@Data
public class Student {
    private Integer id;
    private Integer classId;
    private String studentName;
}
```

### 创建仓储

```java
@RootRepository(scanPackages = "xxx.xxx.xxx.query")
public class SchoolRepository extends MybatisPlusGenericRepository<School, Integer> {
}
```

```java
@RootRepository
public class ClassRepository extends MybatisPlusGenericRepository<Class, Integer> {
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

## 动态查询

### 创建查询

```java
package xxx.xxx.xxx.query;

@Data
public class SchoolQuery {
    private String schoolName;
    private String teacherName;
    private String className;
    private String studentName;
    private Boolean model; // 是否校内模范
}
```

### 调用仓储

```java
@Service
@AllArgsConstructor
public class SchoolServiceImpl implements SchoolService {

    private final SchoolRepository schoolRepository;

    @Override
    public List<School> querySchoolsByDto(SchoolQuery dto) {
        BoundedContext boundedContext = new BoundedContext();
        EntityExample entityExample = schoolRepository.buildExample(boundedContext, dto);
        Object example = entityExample.buildExample();
		return schoolRepository.selectByExample(boundedContext, example);
    }
}
```

### 实体变更

![avatar](https://gitee.com/digital-engine/spring-domain/raw/master/static/img/modify.png)

```java
@Data
public class Teacher {
    private Integer id;
    private Integer schoolId;
    private String teacherName;
}
```

```java
@Data
public class Student {
    private Integer id;
    private Integer classId;
    private String studentName;
    private boolean model; // 是否校内模范
}
```

```java
@Service
@AllArgsConstructor
public class SchoolServiceImpl implements SchoolService {

    private final SchoolRepository schoolRepository;

    @Override
    public List<School> querySchoolsByDto(SchoolQuery dto) { // 代码没变
        BoundedContext boundedContext = new BoundedContext();
        EntityExample entityExample = schoolRepository.buildExample(boundedContext, dto);
        Object example = entityExample.buildExample();
		return schoolRepository.selectByExample(boundedContext, example);
    }
}
```

