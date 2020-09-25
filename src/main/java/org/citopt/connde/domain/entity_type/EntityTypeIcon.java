package org.citopt.connde.domain.entity_type;

import org.apache.commons.codec.binary.Base64;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class EntityTypeIcon {

    private String name = "";
    private String content = "";

    public EntityTypeIcon() {

    }

    public EntityTypeIcon(String content) {
        setContent(content);
    }

    public EntityTypeIcon(String name, String content) {
        setName(name);
        setContent(content);
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

    public static EntityTypeIcon fromFile(File file) throws IOException {
        //Sanity check
        if (file == null) {
            throw new IllegalArgumentException("File must not be null.");
        }

        byte[] fileBytes = new byte[(int) file.length()];

        FileInputStream fileInputStreamReader = new FileInputStream(file);
        fileInputStreamReader.read(fileBytes);

        String base64String = new String(Base64.encodeBase64(fileBytes), StandardCharsets.UTF_8);

        EntityTypeIcon icon = new EntityTypeIcon(file.getName(), base64String);

        return icon;
    }
}
