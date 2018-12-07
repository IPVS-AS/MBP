package org.citopt.connde.domain.adapter;

/**
 *
 * @author rafaelkperes
 */
public class Code {

    private String name;
    private String content;

    public static final String SERVICE_EXTENSION = ".conf";

    public Code() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "ScriptFile{" + "name=" + name + ", content=" + content + '}';
    }
}
