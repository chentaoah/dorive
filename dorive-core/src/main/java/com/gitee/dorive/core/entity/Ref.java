package com.gitee.dorive.core.entity;

import com.gitee.dorive.core.api.Task;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Data
@AllArgsConstructor
public class Ref {

    private BoundedContext boundedContext;
    private Object object;

    public Object execute(String name, String... namesToAdd) {
        Map<String, Task> tasks = boundedContext.getTasks();
        Task task = tasks.get(name);
        if (task != null) {
            boundedContext.appendNames(namesToAdd);
            return task.execute(object);
        }
        return null;
    }

    public CompletableFuture<Object> async(String name, String... namesToAdd) {
        Map<String, Task> tasks = boundedContext.getTasks();
        Task task = tasks.get(name);
        if (task != null) {
            boundedContext.appendNames(namesToAdd);
            return CompletableFuture.supplyAsync(() -> task.execute(object));
        }
        return null;
    }

}
