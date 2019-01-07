package org.citopt.connde.domain.adapter.parameters;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum ParameterTypes {
    TEXT("Text"), NUMBER("Number"), BOOLEAN("Switch");

    private String name;

    ParameterTypes(String name){
        this.name = name;
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
}
