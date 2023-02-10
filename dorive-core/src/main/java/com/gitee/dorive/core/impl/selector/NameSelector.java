package com.gitee.dorive.core.impl.selector;

import cn.hutool.core.util.StrUtil;
import com.gitee.dorive.core.api.Selector;
import com.gitee.dorive.core.entity.BoundedContext;
import com.gitee.dorive.core.entity.definition.EntityDefinition;
import com.gitee.dorive.core.repository.ConfiguredRepository;
import com.gitee.dorive.core.util.StringUtils;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
public class NameSelector implements Selector {

    private boolean wildcard;
    private Map<String, NameDef> nameDefMap = Collections.emptyMap();

    public NameSelector(String... names) {
        if (names != null && names.length > 0) {
            nameDefMap = new LinkedHashMap<>(names.length * 4 / 3 + 1);
            for (String name : names) {
                if ("*".equals(name)) {
                    wildcard = true;
                } else {
                    if (name.contains("(") && name.contains(")")) {
                        String realName = name.substring(0, name.indexOf("("));
                        String propsText = name.substring(name.indexOf("(") + 1, name.indexOf(")"));
                        List<String> columns = StringUtils.toUnderlineCase(StrUtil.splitTrim(propsText, ","));
                        nameDefMap.put(realName, new NameDef(realName, columns));

                    } else {
                        nameDefMap.put(name, new NameDef(name, Collections.emptyList()));
                    }
                }
            }
        }
    }

    @Override
    public boolean isMatch(BoundedContext boundedContext, ConfiguredRepository repository) {
        if (wildcard) {
            return true;
        } else {
            EntityDefinition entityDefinition = repository.getEntityDefinition();
            String name = entityDefinition.getName();
            return org.apache.commons.lang3.StringUtils.isBlank(name) || nameDefMap.containsKey(name);
        }
    }

    @Override
    public List<String> selectColumns(BoundedContext boundedContext, ConfiguredRepository repository) {
        EntityDefinition entityDefinition = repository.getEntityDefinition();
        String name = entityDefinition.getName();
        NameDef nameDef = nameDefMap.get(name);
        return nameDef != null && !nameDef.getColumns().isEmpty() ? nameDef.getColumns() : null;
    }

    @Data
    @AllArgsConstructor
    public static class NameDef {
        private String name;
        private List<String> columns;
    }

}

