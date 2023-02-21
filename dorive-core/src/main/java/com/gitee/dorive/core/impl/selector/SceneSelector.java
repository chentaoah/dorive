package com.gitee.dorive.core.impl.selector;

import cn.hutool.core.collection.CollUtil;
import com.gitee.dorive.core.api.Selector;
import com.gitee.dorive.core.entity.BoundedContext;
import com.gitee.dorive.core.entity.definition.EntityDefinition;
import com.gitee.dorive.core.repository.CommonRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SceneSelector implements Selector {

    private Set<String> scenes = Collections.emptySet();

    public SceneSelector(String... scenes) {
        if (scenes != null && scenes.length > 0) {
            this.scenes = CollUtil.set(false, scenes);
        }
    }

    @Override
    public boolean isMatch(BoundedContext boundedContext, CommonRepository repository) {
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
    public boolean isRelay(BoundedContext boundedContext, CommonRepository repository) {
        return false;
    }

    @Override
    public List<String> selectColumns(BoundedContext boundedContext, CommonRepository repository) {
        return null;
    }

}
