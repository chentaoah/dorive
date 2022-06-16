package com.gitee.spring.boot.starter.domain.builder;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.gitee.spring.boot.starter.domain.api.EntityCriterionBuilder;
import com.gitee.spring.domain.core.api.EntityCriterion;
import com.gitee.spring.domain.core.constants.Operator;
import com.gitee.spring.domain.core.entity.AbstractEntityCriterion;
import com.gitee.spring.domain.core.entity.EntityExample;

public class GEEntityCriterionBuilder implements EntityCriterionBuilder {

    @Override
    public EntityCriterion newCriterion(String fieldName, Object fieldValue) {
        return new AbstractEntityCriterion(fieldName, Operator.GE, fieldValue) {
            @Override
            public void appendTo(EntityExample entityExample) {
                QueryWrapper<?> queryWrapper = (QueryWrapper<?>) entityExample.getExample();
                String fieldName = StrUtil.toUnderlineCase(this.fieldName);
                queryWrapper.ge(fieldName, fieldValue);
            }
        };
    }

}
