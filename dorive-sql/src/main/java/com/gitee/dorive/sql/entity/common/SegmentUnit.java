package com.gitee.dorive.sql.entity.common;

import com.gitee.dorive.query.entity.QueryUnit;
import com.gitee.dorive.sql.entity.segment.TableSegment;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SegmentUnit extends QueryUnit {

    private TableSegment tableSegment;

    public String getTableAlias() {
        return tableSegment.getTableAlias();
    }

}
