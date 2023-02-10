package com.gitee.dorive.core.impl.selector;

import com.gitee.dorive.core.api.Selector;
import com.gitee.dorive.core.entity.BoundedContext;
import com.gitee.dorive.core.entity.definition.EntityDefinition;
import com.gitee.dorive.core.repository.ConfiguredRepository;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
@AllArgsConstructor
public class SceneSelector implements Selector {

    private Set<String> scenes;

    @Override
    public boolean isMatch(BoundedContext boundedContext, ConfiguredRepository repository) {
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
    public List<String> selectColumns(BoundedContext boundedContext, ConfiguredRepository repository) {
        return null;
    }

}
