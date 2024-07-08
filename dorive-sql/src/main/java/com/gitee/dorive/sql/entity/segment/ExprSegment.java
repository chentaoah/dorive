package com.gitee.dorive.sql.entity.segment;

import com.gitee.dorive.sql.api.Segment;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ExprSegment implements Segment {

    private String leftExpr;
    private String operator;
    private String rightExpr;

    @Override
    public String toString() {
        if (rightExpr != null) {
            return leftExpr + " " + operator + " " + rightExpr;
        } else {
            return leftExpr + " " + operator;
        }
    }

}
