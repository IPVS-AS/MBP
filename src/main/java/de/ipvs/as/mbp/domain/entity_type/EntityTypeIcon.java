package de.ipvs.as.mbp.domain.entity_type;

import org.apache.commons.codec.binary.Base64;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class EntityTypeIcon {

    //Prefix for base64 encoded files
    private static final String REGEX_BASE64_PREFIX = "^data:[a-zA-Z0-9/,\\-]*;base64,";

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

    /**
     * Returns the rough real size of the base64 encoded content in bytes.
     * The calculation is based on https://stackoverflow.com/a/34109103/11210504
     *
     * @return The size of the content in bytes
     */
    public long getSize() {
        return content.getBytes().length * 3L / 4;
    }

    /**
     * Returns the icon as buffered image or null if the content cannot be converted to an image object.
     *
     * @return The resulting buffered image or null
     */
    public BufferedImage toImageObject() {
        //Check if content is available
        if (content == null || content.isEmpty()) {
            return null;
        }

        //Replace MIME prefix from base64 content string
        String prefixlessContent = content.replaceAll(REGEX_BASE64_PREFIX, "");

        try {
            //Decode base64 string to bytes
            byte[] imageBytes = Base64.decodeBase64(prefixlessContent);

            //Create buffered image from bytes
            return ImageIO.read(new ByteArrayInputStream(imageBytes));
        } catch (Exception ignored) {
            return null;
        }
    }
}
