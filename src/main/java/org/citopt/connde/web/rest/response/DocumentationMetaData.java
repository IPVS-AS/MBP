package org.citopt.connde.web.rest.response;

import java.util.List;

/**
 * Object holding meta data about the documentation of the REST interface.
 */
public class DocumentationMetaData {

    //Context path of the application
    private String contextPath = "";

    //Documentation URLs
    private List<DocumentationURL> swaggerURLs;
    private List<DocumentationURL> exportURLs;

    /**
     * Creates a new documentation meta data object by passing a list of swagger URLs and a list of export URLs.
     *
     * @param swaggerURLs A list containing swagger-related documentation URLs and descriptions
     * @param exportURLs  A mlist containing URLs and descriptions for exporting the documentation
     */
    public DocumentationMetaData(List<DocumentationURL> swaggerURLs, List<DocumentationURL> exportURLs) {
        this.swaggerURLs = swaggerURLs;
        this.exportURLs = exportURLs;
    }

    /**
     * Returns the context path of the application.
     *
     * @return The context path
     */
    public String getContextPath() {
        return contextPath;
    }

    /**
     * Sets the context path that is used by the application.
     *
     * @param contextPath The context path to set
     */
    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }

    /**
     * Returns a list containing swagger-related documentation URLs and descriptions.
     *
     * @return The list of URLs
     */
    public List<DocumentationURL> getSwaggerURLs() {
        return swaggerURLs;
    }

    /**
     * Sets a list of swagger-related documentation URLs and descriptions.
     *
     * @param swaggerURLs The list of URLs to set
     */
    public void setSwaggerURLs(List<DocumentationURL> swaggerURLs) {
        this.swaggerURLs = swaggerURLs;
    }

    /**
     * Returns a list containing URLs and descriptions for exporting the documentation.
     *
     * @return The list of URLs
     */
    public List<DocumentationURL> getExportURLs() {
        return exportURLs;
    }

    /**
     * Sets a list of URLs and descriptions for exporting the documentation.
     *
     * @param exportURLs The list of URLs
     */
    public void setExportURLs(List<DocumentationURL> exportURLs) {
        this.exportURLs = exportURLs;
    }
}
