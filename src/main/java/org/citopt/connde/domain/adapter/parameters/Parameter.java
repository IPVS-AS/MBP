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

    public boolean isInstanceValid(ParameterInstance instance){
        //Check name
        if(!instance.getName().equals(this.name)){
            return false;
        }

        //Check data type
        Object value = instance.getValue();
        switch(this.type){
            case BOOLEAN:
                if(!(value instanceof Boolean)){
                    return false;
                }
                break;
            case NUMBER:
                if(!((value instanceof Double) || (value instanceof Integer))){
                    return false;
                }
                break;
            case TEXT:
                if(!(value instanceof String)){
                    return false;
                }
                String stringValue = (String) value;
                if(this.mandatory && stringValue.isEmpty()){
                    return false;
                }
                break;
        }
        return true;
    }
}
