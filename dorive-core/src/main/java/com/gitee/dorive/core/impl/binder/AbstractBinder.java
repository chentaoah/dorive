package com.gitee.dorive.core.impl.binder;

import com.gitee.dorive.api.entity.def.BindingDef;
import com.gitee.dorive.core.api.binder.Binder;
import com.gitee.dorive.core.api.binder.Processor;
import com.gitee.dorive.core.api.context.Context;
import com.gitee.dorive.core.impl.endpoint.BindEndpoint;
import com.gitee.dorive.core.impl.endpoint.FieldEndpoint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public abstract class AbstractBinder implements Binder {

    protected BindingDef bindingDef;
    protected FieldEndpoint fieldEndpoint;
    protected BindEndpoint bindEndpoint;
    protected Processor processor;

    public String getFieldName() {
        return fieldEndpoint.getFieldElement().getFieldName();
    }

    public Object getFieldValue(Context context, Object entity) {
        return fieldEndpoint.getValue(entity);
    }

    public void setFieldValue(Context context, Object entity, Object value) {
        fieldEndpoint.setValue(entity, value);
    }

    public String getBoundName() {
        return bindEndpoint.getFieldElement().getFieldName();
    }

    public Object getBoundValue(Context context, Object entity) {
        return bindEndpoint.getValue(entity);
    }

    public void setBoundValue(Context context, Object entity, Object value) {
        bindEndpoint.setValue(entity, value);
    }

    @Override
    public Object input(Context context, Object value) {
        return value == null || processor == null ? value : processor.input(context, value);
    }

    @Override
    public Object output(Context context, Object value) {
        return value == null || processor == null ? value : processor.output(context, value);
    }

    public String getBindField() {
        return bindingDef.getBindField();
    }

    public String getAlias() {
        return fieldEndpoint.getAlias();
    }

    public boolean isCollection() {
        return bindEndpoint.getFieldElement().isCollection();
    }

    public String getBelongAccessPath() {
        return bindEndpoint.getBelongAccessPath();
    }

    public String getBindAlias() {
        return bindEndpoint.getBindAlias();
    }

    public boolean isSameType() {
        return fieldEndpoint.isSameType(bindEndpoint);
    }

}
