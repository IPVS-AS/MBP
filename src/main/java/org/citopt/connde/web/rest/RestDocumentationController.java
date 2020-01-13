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

/**
 * REST controller for serving various data related to the documentation of the REST interface.
 */
@RestController
@RequestMapping(RestConfiguration.BASE_PATH)
@Api(tags = {"Documentation"}, description = "Provides documentation for the REST interface in different formats")
public class RestDocumentationController {

    private static final Swagger2MarkupConfig CONVERTER_CONFIG = new Swagger2MarkupConfigBuilder()
            .withMarkupLanguage(MarkupLanguage.MARKDOWN)
            .withOutputLanguage(Language.EN)
            .withPathsGroupedBy(GroupBy.TAGS)
            .build();

    @GetMapping(value = "/doc/markdown")
    @Secured({Constants.ADMIN})
    @ApiOperation(value = "Provides the documentation in Markdown format", produces = "text/markdown")
    @ApiResponses({@ApiResponse(code = 200, message = "Success"), @ApiResponse(code = 403, message = "Not authorized to access the documentation")})
    public ResponseEntity<String> exportDocumentation() throws MalformedURLException {
        String jsonPath = ServletUriComponentsBuilder.fromCurrentContextPath().toUriString() + SwaggerConfiguration.SWAGGER_PATH_JSON;

        URL remoteSwaggerFile = new URL(jsonPath);
        String documentation = Swagger2MarkupConverter.from(remoteSwaggerFile)
                .withConfig(CONVERTER_CONFIG)
                .build()
                .toString();

        return ResponseEntity.ok()
                .contentType(new MediaType("text", "markdown", StandardCharsets.UTF_8))
                .body(documentation);
    }
}
