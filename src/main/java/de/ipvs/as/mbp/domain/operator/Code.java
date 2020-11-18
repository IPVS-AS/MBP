package de.ipvs.as.mbp.domain.operator;

import org.apache.commons.codec.binary.Base64;
import de.ipvs.as.mbp.util.CryptoUtils;

public class Code {

    //Prefix for base64 encoded files
    private static final String REGEX_BASE64_PREFIX = "^data:[a-zA-Z0-9/,\\-]*;base64,";

    private String name;
    private String content;
    private String hash;

    public Code() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContent() {
        //Check if content is base64 encoded
        if (isBase64Encoded()) {
            //Replace base64 prefix and return only the base64 content
            return content.replaceAll(REGEX_BASE64_PREFIX, "");
        }
        //Otherwise just return the content as it is
        return content;
    }

    public void setContent(String content) {
        this.content = content;

        //Update hash
        updateContentHash();
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public boolean isBase64Encoded() {
        //Sanity check
        if (content == null) {
            return false;
        }

        //Check if content has the base64 prefix
        return content.matches(REGEX_BASE64_PREFIX + ".+");
    }

    public void updateContentHash() {
        //Sanity check
        if ((content == null) || content.isEmpty()) {
            hash = "";
            return;
        }

        //Check if content is base64 encoded
        if (isBase64Encoded()) {
            //Decode from Base64
            byte[] decodedBytes = Base64.decodeBase64(getContent());

            //Hash the decoded content
            hash = CryptoUtils.md5(decodedBytes);
        } else {
            //Not encoded, just hash the content
            hash = CryptoUtils.md5(content);
        }
    }
}
