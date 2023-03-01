package com.gitee.dorive.core.impl.selector;

import cn.hutool.core.collection.CollUtil;
import com.gitee.dorive.core.entity.definition.EntityDefinition;
import com.gitee.dorive.core.repository.CommonRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class SceneSelector extends AbstractSelector {

    private Set<String> scenes = Collections.emptySet();

    public SceneSelector(String... scenes) {
        if (scenes != null && scenes.length > 0) {
            this.scenes = CollUtil.set(false, scenes);
        }
    }

    @Override
    public boolean matches(CommonRepository repository) {
        EntityDefinition entityDefinition = repository.getEntityDefinition();
        String[] scenes = entityDefinition.getScenes();
        if (scenes == null || scenes.length == 0) {
            return true;
        }
        for (String scene : scenes) {
            if (this.scenes.contains(scene)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<String> selectColumns(CommonRepository repository) {
        return null;
    }

}
