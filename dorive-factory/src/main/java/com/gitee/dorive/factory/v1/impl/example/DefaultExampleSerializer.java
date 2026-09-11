package com.gitee.dorive.factory.v1.impl.example;

import com.gitee.dorive.base.v1.definition.constant.Operator;
import com.gitee.dorive.base.v1.executor.api.Context;
import com.gitee.dorive.base.v1.executor.entity.qry.Criterion;
import com.gitee.dorive.base.v1.executor.entity.qry.Example;
import com.gitee.dorive.base.v1.executor.entity.qry.OrderBy;
import com.gitee.dorive.base.v1.executor.util.MultiInBuilder;
import com.gitee.dorive.base.v1.factory.api.entity.EntityMapper;
import com.gitee.dorive.base.v1.factory.api.example.ExampleSerializer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class DefaultExampleSerializer implements ExampleSerializer {

    private final EntityMapper entityMapper;

    @Override
    public void serialize(Context context, Example example) {
        serializeSelectProps(example);
        serialize(context, example.getCriteria());
        serialize(example.getOrderBy());
    }

    private void serializeSelectProps(Example example) {
        List<String> properties = example.getSelectProps();
        if (properties != null && !properties.isEmpty()) {
            properties = entityMapper.serialize(properties);
            example.setSelectProps(properties);
        }
    }

    @Override
    public void serialize(Context context, List<Criterion> criteria) {
        if (criteria != null && !criteria.isEmpty()) {
            for (Criterion criterion : criteria) {
                String operator = criterion.getOperator();
                if (Operator.AND.equals(operator) || Operator.OR.equals(operator)) {
                    Object value = criterion.getValue();
                    if (value instanceof Example) {
                        serialize(context, (Example) value);
                    }
                } else if (Operator.MULTI_IN.equals(operator)) {
                    Object value = criterion.getValue();
                    if (value instanceof MultiInBuilder builder) {
                        List<String> properties = builder.getProperties();
                        properties = entityMapper.serialize(properties);
                        builder.setProperties(properties);
                        criterion.setProperty(builder.buildPropertiesStr());
                        criterion.setValue(builder.buildValuesStr());
                    }
                } else {
                    serialize(criterion);
                }
            }
        }
    }

    private void serialize(Criterion criterion) {
        String property = criterion.getProperty();
        Object value = criterion.getValue();
        // 如果是field
        String alias = entityMapper.serialize(property);
        if (alias != null) {
            value = entityMapper.serialize(property, value);
            property = alias;
        }
        // 重新设置回去
        criterion.setProperty(property);
        criterion.setValue(value);
    }

    private void serialize(OrderBy orderBy) {
        if (orderBy != null) {
            List<String> properties = orderBy.getProperties();
            properties = entityMapper.serialize(properties);
            orderBy.setProperties(properties);
        }
    }

}
