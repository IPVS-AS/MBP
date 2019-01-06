package org.citopt.connde.domain.adapter.parameters;

public enum ParameterTypes {
    TEXT("Text"), NUMBER("Number"), BOOLEAN("Switch");

    private String name;

    ParameterTypes(String name){
        this.name = name;
    }
}
