package com.gitee.spring.domain.core.property;

import com.gitee.spring.domain.core.api.EntityMapper;
import com.gitee.spring.domain.core.api.ParameterConverter;
import com.gitee.spring.domain.core.entity.BoundedContext;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class DefaultEntityMapper implements EntityMapper {

    protected EntityMapper entityMapper;
    protected ParameterConverter parameterConverter;

    @Override
    public Object newPage(Integer pageNum, Integer pageSize) {
        return entityMapper.newPage(pageNum, pageSize);
    }

    @Override
    public List<?> getDataFromPage(Object dataPage) {
        return entityMapper.getDataFromPage(dataPage);
    }

    @Override
    public Object newPageOfEntities(Object dataPage, List<Object> entities) {
        return entityMapper.newPageOfEntities(dataPage, entities);
    }

    @Override
    public Object newExample(BoundedContext boundedContext) {
        return entityMapper.newExample(boundedContext);
    }

    @Override
    public void addToExample(Object example, String fieldAttribute, Object boundValue) {
        if (parameterConverter != null) {
            fieldAttribute = parameterConverter.convertFieldName(fieldAttribute, boundValue);
            boundValue = parameterConverter.convertFieldValue(fieldAttribute, boundValue);
        }
        entityMapper.addToExample(example, fieldAttribute, boundValue);
    }

}
