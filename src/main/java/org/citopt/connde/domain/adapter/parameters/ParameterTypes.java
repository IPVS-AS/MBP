package org.citopt.connde.domain.adapter.parameters;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ParameterTypes {
    TEXT("Text"), NUMBER("Number"), BOOLEAN("Switch");

    private String value;

    ParameterTypes(String value){
        this.value = value;
    }

    @Override
    public String toString(){
        return this.value;
    }

    @JsonCreator
    public static ParameterTypes create(String value) {
        if(value == null) {
            return null;
        }
        for(ParameterTypes type : values()) {
            if(value.equals(type.toString())) {
                return type;
            }
        }
        return null;
    }

    @JsonValue
    public String toValue() {
        return this.value;
    }
}
