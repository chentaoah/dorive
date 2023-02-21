package com.gitee.dorive.core.impl.selector;

import cn.hutool.core.util.StrUtil;
import com.gitee.dorive.core.api.Selector;
import com.gitee.dorive.core.entity.BoundedContext;
import com.gitee.dorive.core.entity.definition.EntityDefinition;
import com.gitee.dorive.core.repository.CommonRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
public class NameSelector implements Selector {

    public static final NameSelector EMPTY_SELECTOR = new NameSelector();

    private boolean wildcard;
    private Map<String, NameDef> nameDefMap = Collections.emptyMap();

    public NameSelector(String... names) {
        if (names != null && names.length > 0) {
            this.nameDefMap = new LinkedHashMap<>(names.length * 4 / 3 + 1);
            for (String name : names) {
                if ("*".equals(name)) {
                    this.wildcard = true;
                } else {
                    boolean isRelay = name.startsWith("$");
                    if (isRelay) {
                        name = name.substring(1);
                    }
                    if (name.contains("(") && name.contains(")")) {
                        String realName = name.substring(0, name.indexOf("("));
                        String propertiesText = name.substring(name.indexOf("(") + 1, name.indexOf(")"));
                        List<String> properties = StrUtil.splitTrim(propertiesText, ",");
                        nameDefMap.put(realName, new NameDef(isRelay, realName, properties));

                    } else {
                        nameDefMap.put(name, new NameDef(isRelay, name, Collections.emptyList()));
                    }
                }
            }
        }
    }

    @Override
    public boolean isMatch(BoundedContext boundedContext, CommonRepository repository) {
        if (wildcard) {
            return true;
        }
        EntityDefinition entityDefinition = repository.getEntityDefinition();
        String name = entityDefinition.getName();
        return StringUtils.isBlank(name) || nameDefMap.containsKey(name);
    }

    @Override
    public boolean isRelay(BoundedContext boundedContext, CommonRepository repository) {
        EntityDefinition entityDefinition = repository.getEntityDefinition();
        String name = entityDefinition.getName();
        NameDef nameDef = nameDefMap.get(name);
        return nameDef != null && nameDef.isRelay();
    }

    @Override
    public List<String> selectColumns(BoundedContext boundedContext, CommonRepository repository) {
        EntityDefinition entityDefinition = repository.getEntityDefinition();
        String name = entityDefinition.getName();
        NameDef nameDef = nameDefMap.get(name);
        return nameDef != null && !nameDef.getProperties().isEmpty() ? nameDef.getProperties() : null;
    }

    @Data
    @AllArgsConstructor
    public static class NameDef {
        private boolean relay;
        private String name;
        private List<String> properties;
    }

}

