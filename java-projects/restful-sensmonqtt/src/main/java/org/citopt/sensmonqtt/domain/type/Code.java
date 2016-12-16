package org.citopt.sensmonqtt.domain.type;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.stereotype.Component;

/**
 *
 * @author rafaelkperes
 */
@Component
public class Code {

    String name;
    byte[] content;

    static final String SERVICE_EXTENSION = ".conf";

    @PersistenceConstructor
    public Code(String name, byte[] content) {
        this.name = name;
        this.content = content;
    }

    public Code(ObjectId id, byte[] content) {
        this.name = id.toString() + SERVICE_EXTENSION;
        this.content = content;
    }

    public Code() {
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
