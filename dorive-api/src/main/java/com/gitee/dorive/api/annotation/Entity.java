/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gitee.dorive.api.annotation;

import java.lang.annotation.*;

/**
 * 实体注解<br>
 * 作用：声明一个类或字段的类型为实体<br>
 * <p>
 * 解释：实体是一种数据结构的具体表现形式，具有以下特征：<br>
 * 1、描述了实体和其他实体之间的关系。（一对一、一对多、多对多）<br>
 * 2、描述了实体字段和持久化数据的映射关系。<br>
 * 3、包含方法，可通过方法重写进行拓展。<br>
 * </p>
 * <p>
 * 使用说明：<br>
 * 1、在类上声明的实体注解，代表该实体的默认配置<br>
 * <pre>{@code
 * @Entity(name = "user", source = UserMapper.class)
 * public class User {
 *     ......
 * }
 * }</pre>
 * 2、在字段上声明的实体注解，代表在当前实体内的配置<br>
 * <pre>{@code
 * public class Dept {
 *     @Entity(name = "user1", source = UserMapper1.class)
 *     private List<User> users;
 * }
 * }</pre>
 * 3、一般情况下，使用默认配置即可<br>
 * <pre>{@code
 * public class Dept {
 *     @Entity
 *     private List<User> users;
 * }
 * }</pre>
 * </p>
 * <p>
 * 补充说明：如果想要在关联查询时，查询实体内部的其他实体，请指定仓储<br>
 * <pre>{@code
 * @Entity(name = "dept", source = DeptMapper.class)
 * public class Dept {
 *     @Entity(repository = UserRepository.class)
 *     private List<User> users;
 * }
 *
 * @Entity(name = "user", source = UserMapper.class)
 * public class User {
 *     @Entity
 *     private List<Role> roles;
 * }
 * }</pre>
 *
 * @author tao.chen
 * @see Aggregate
 * </p>
 */
@Inherited
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD})
public @interface Entity {

    /**
     * 实体名称<br>
     * 作用：可作为选取器的匹配条件<br>
     */
    String name() default "";

    /**
     * 数据来源<br>
     * 作用：声明持久化数据操作的具体实现<br>
     * 注意：目前仅支持mybatis-plus框架中BaseMapper的子类<br>
     */
    Class<?> source() default Object.class;

    /**
     * 实体工厂<br>
     * 作用：声明实体构造的具体实现<br>
     * 解释：实体工厂通过映射关系，实现了实体与持久化数据之间的相互转换<br>
     * 多态的实现方式：<br>
     * 1、假设实体为User，子类分别为User1、User2<br>
     * 2、新建UserFactory继承于DefaultEntityFactory，并重写reconstitute方法<br>
     * <pre>{@code
     * @Override
     * public Object reconstitute(Context context, Object persistent) {
     *     User user = (User) super.reconstitute(context, persistent);
     *     if (user.getType() == 1) {
     *         return BeanUtil.copyProperties(user, User1.class);
     *
     *     } else if (user.getType() == 2) {
     *         return BeanUtil.copyProperties(user, User2.class);
     *     }
     *     return user;
     * }
     * }</pre>
     * 3、修改User的@Entity注解，指定实体工厂为UserFactory<br>
     * <pre>{@code
     * @Entity(......, factory = UserFactory.class)
     * public class User {
     *     ......
     * }
     * }</pre>
     * 4、在UserRepository中引入子类的仓储<br>
     * <pre>{@code
     * public class UserRepository extends MybatisPlusRepository<User, Integer> {
     *     private final User1Repository user1Repository;
     *     private final User2Repository user2Repository;
     * }
     * }</pre>
     */
    Class<?> factory() default Object.class;

    /**
     * 指定仓储<br>
     * 作用：指定一个特定的仓储，代替source操作数据<br>
     * 注意：不能和source与factory同时使用，优先级更高<br>
     */
    Class<?> repository() default Object.class;

    /**
     * 操作优先级<br>
     * 作用：可以改变插入、更新、删除的操作顺序<br>
     */
    int priority() default 0;

    /**
     * 默认排序字段<br>
     * 作用：关联查询时，默认的排序字段<br>
     */
    String sortBy() default "";

    /**
     * 默认排序方式<br>
     * 作用：关联查询时，默认的排序方式<br>
     *
     * @see com.gitee.dorive.api.constant.Order
     */
    String order() default "";

}

