package org.citopt.websensor.domain;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.stereotype.Component;

@Component
public class ScriptFile {

    String name;
    byte[] content;

    static final String SERVICE_EXTENSION = ".conf";

    @PersistenceConstructor
    public ScriptFile(String name, byte[] content) {
        this.name = name;
        this.content = content;
    }

    public ScriptFile(ObjectId id, byte[] content) {
        this.name = id.toString() + SERVICE_EXTENSION;
        this.content = content;
    }

    public ScriptFile() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "ScriptFile{" + "name=" + name + ", content=" + content + '}';
    }

}
