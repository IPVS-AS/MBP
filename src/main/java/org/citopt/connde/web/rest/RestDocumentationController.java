package org.citopt.connde.web.rest;

import io.github.swagger2markup.GroupBy;
import io.github.swagger2markup.Language;
import io.github.swagger2markup.Swagger2MarkupConfig;
import io.github.swagger2markup.Swagger2MarkupConverter;
import io.github.swagger2markup.builder.Swagger2MarkupConfigBuilder;
import io.github.swagger2markup.markup.builder.MarkupLanguage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.citopt.connde.RestConfiguration;
import org.citopt.connde.SwaggerConfiguration;
import org.citopt.connde.constants.Constants;
import org.citopt.connde.web.rest.response.DocumentationMetaData;
import org.citopt.connde.web.rest.response.DocumentationURL;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * REST controller for serving various data related to the documentation of the REST interface.
 */
@RestController
@RequestMapping(RestConfiguration.BASE_PATH)
@Api(tags = {"Documentation"}, description = "Provides documentation for the REST interface in different formats")
public class RestDocumentationController {

    //Export paths for documentation
    private static final String EXPORT_PATH_ASCIIDOC = "/docs/asciidoc";
    private static final String EXPORT_PATH_MARKDOWN = "/docs/markdown";

    //Meta data about the documentation of the REST interface
    private static final DocumentationMetaData DOCUMENTATION_META_DATA = new DocumentationMetaData(Arrays.asList(
            new DocumentationURL(SwaggerConfiguration.SWAGGER_PATH_UI, "Swagger UI"),
            new DocumentationURL(SwaggerConfiguration.SWAGGER_PATH_JSON, "Swagger JSON API")),
            Arrays.asList(
                    new DocumentationURL(RestConfiguration.BASE_PATH + EXPORT_PATH_ASCIIDOC, "AsciiDoc"),
                    new DocumentationURL(RestConfiguration.BASE_PATH + EXPORT_PATH_MARKDOWN, "Markdown")
            ));


    @GetMapping(value = "/docs")
    @Secured({Constants.ADMIN})
    @ApiOperation(value = "Retrieves meta data and descriptions about the documentation", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success"), @ApiResponse(code = 403, message = "Not authorized to access the meta data")})
    public ResponseEntity<DocumentationMetaData> getDocumentationURLs() {
        //Determine context path of the application
        String contextPath = ServletUriComponentsBuilder.fromCurrentContextPath().toUriString();

        //Set context path as meta data
        DOCUMENTATION_META_DATA.setContextPath(contextPath);

        //Just reply with the meta data object
        return new ResponseEntity<>(DOCUMENTATION_META_DATA, HttpStatus.OK);
    }

    @GetMapping(value = EXPORT_PATH_ASCIIDOC)
    @Secured({Constants.ADMIN})
    @ApiOperation(value = "Provides the documentation in AsciiDoc format", produces = "text/asciidoc")
    @ApiResponses({@ApiResponse(code = 200, message = "Success"), @ApiResponse(code = 403, message = "Not authorized to access the documentation")})
    public ResponseEntity<String> exportAsciiDoc() throws MalformedURLException {
        URL swaggerJSONURL = new URL(ServletUriComponentsBuilder.fromCurrentContextPath().toUriString() + SwaggerConfiguration.SWAGGER_PATH_JSON);

        //Build config for markdown
        Swagger2MarkupConfig config = new Swagger2MarkupConfigBuilder()
                .withMarkupLanguage(MarkupLanguage.ASCIIDOC)
                .withOutputLanguage(Language.EN)
                .withPathsGroupedBy(GroupBy.TAGS)
                .build();

        String documentation = Swagger2MarkupConverter.from(swaggerJSONURL)
                .withConfig(config)
                .build()
                .toString();

        return ResponseEntity.ok()
                .contentType(new MediaType("text", "asciidoc", StandardCharsets.UTF_8))
                .body(documentation);
    }

    @GetMapping(value = EXPORT_PATH_MARKDOWN)
    @Secured({Constants.ADMIN})
    @ApiOperation(value = "Provides the documentation in Markdown format", produces = "text/markdown")
    @ApiResponses({@ApiResponse(code = 200, message = "Success"), @ApiResponse(code = 403, message = "Not authorized to access the documentation")})
    public ResponseEntity<String> exportMarkdown() throws MalformedURLException {
        URL swaggerJSONURL = new URL(ServletUriComponentsBuilder.fromCurrentContextPath().toUriString() + SwaggerConfiguration.SWAGGER_PATH_JSON);

        //Build config for markdown
        Swagger2MarkupConfig config = new Swagger2MarkupConfigBuilder()
                .withMarkupLanguage(MarkupLanguage.MARKDOWN)
                .withOutputLanguage(Language.EN)
                .withPathsGroupedBy(GroupBy.TAGS)
                .build();

        String documentation = Swagger2MarkupConverter.from(swaggerJSONURL)
                .withConfig(config)
                .build()
                .toString();

        return ResponseEntity.ok()
                .contentType(new MediaType("text", "markdown", StandardCharsets.UTF_8))
                .body(documentation);
    }
}