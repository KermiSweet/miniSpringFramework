package com.kermi.spring.core;

import com.kermi.spring.constants.ScopType;

public class BeanDefinition {
    private Class type;
    private String name;
    private ScopType scope;

    public ScopType getScope() {
        return scope;
    }

    public void setScope(ScopType scope) {
        this.scope = scope;
    }

    public Class getType() {
        return type;
    }

    public void setType(Class type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
