package org.citopt.connde.web.rest.response;

/**
 * Wraps documentation URLs and their description into one object.
 */
public class DocumentationURL {
    private String url;
    private String description;

    /**
     * Creates a new documentation URL object by using a URL and its description.
     *
     * @param url         The URL
     * @param description The description
     */
    public DocumentationURL(String url, String description) {
        this.description = description;
        this.url = url;
    }

    /**
     * Returns the URL.
     *
     * @return The URL
     */
    public String getUrl() {
        return url;
    }

    /**
     * Sets the URL.
     *
     * @param url The URL to set
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * Returns the description.
     *
     * @return The description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description.
     *
     * @param description The description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }
}
