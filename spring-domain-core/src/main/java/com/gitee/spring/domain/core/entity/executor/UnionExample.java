package com.gitee.spring.domain.core.entity.executor;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class UnionExample extends Example {

    private List<Example> examples = new ArrayList<>();

    public void mergeExample(Example example) {
        examples.add(example);
    }

}
