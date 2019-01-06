package org.citopt.connde.domain.adapter.parameters;


public class Parameter {
    private String name;
    private ParameterTypes type;
    private boolean mandatory;

    public Parameter(){

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ParameterTypes getType() {
        return type;
    }

    public void setType(ParameterTypes type) {
        this.type = type;
    }

    public boolean isMandatory() {
        return mandatory;
    }

    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }
}
