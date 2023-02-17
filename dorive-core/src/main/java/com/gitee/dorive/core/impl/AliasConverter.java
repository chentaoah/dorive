package com.gitee.dorive.core.impl;

import com.gitee.dorive.core.entity.element.EntityElement;
import com.gitee.dorive.core.entity.executor.Criterion;
import com.gitee.dorive.core.entity.executor.Example;
import com.gitee.dorive.core.entity.executor.OrderBy;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class AliasConverter {

    private EntityElement entityElement;

    public void convert(Example example) {
        List<String> selectColumns = example.getSelectColumns();
        selectColumns = entityElement.toAliases(selectColumns);
        example.selectColumns(selectColumns);

        List<Criterion> criteria = example.getCriteria();
        if (criteria != null && !criteria.isEmpty()) {
            for (Criterion criterion : criteria) {
                String property = criterion.getProperty();
                property = entityElement.toAlias(property);
                criterion.setProperty(property);
            }
        }

        OrderBy orderBy = example.getOrderBy();
        if (orderBy != null) {
            List<String> orderByColumns = orderBy.getColumns();
            orderByColumns = entityElement.toAliases(orderByColumns);
            orderBy.setColumns(orderByColumns);
        }
    }

}
