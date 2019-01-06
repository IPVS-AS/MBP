package org.citopt.connde.domain.adapter.parameters;


public class ParameterInstance {
    private String name;
    private Object value;

    public ParameterInstance() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
