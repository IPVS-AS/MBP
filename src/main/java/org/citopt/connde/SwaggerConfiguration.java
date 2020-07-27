package org.citopt.connde;

import java.util.Collections;

import org.citopt.connde.util.ValidationErrorCollection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.fasterxml.classmate.TypeResolver;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiOperation;
import springfox.documentation.annotations.ApiIgnore;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.data.rest.configuration.SpringDataRestConfiguration;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.web.DocExpansion;
import springfox.documentation.swagger.web.ModelRendering;
import springfox.documentation.swagger.web.OperationsSorter;
import springfox.documentation.swagger.web.TagsSorter;
import springfox.documentation.swagger.web.UiConfiguration;
import springfox.documentation.swagger.web.UiConfigurationBuilder;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * Configuration for the swagger documentation generator.
 */
@Import({SpringDataRestConfiguration.class})
@Configuration
@EnableSwagger2
public class SwaggerConfiguration {
    public static final String SWAGGER_PATH_UI = "/swagger-ui.html";
    public static final String SWAGGER_PATH_JSON = "/v2/api-docs";

    /**
     * Creates a docket bean that holds the swagger configuration.
     *
     * @return The docket bean
     */
    @Bean
    public Docket docket() {
        //Type resolver for working with types
        TypeResolver typeResolver = new TypeResolver();
        
        //Create bean
        return new Docket(DocumentationType.SWAGGER_2)
                .useDefaultResponseMessages(false)
                .additionalModels(typeResolver.resolve(ValidationErrorCollection.class))
                .select()
				.apis(RequestHandlerSelectors.withClassAnnotation(Api.class)
							.or(RequestHandlerSelectors.withClassAnnotation(ApiModel.class)
							.or(RequestHandlerSelectors.withMethodAnnotation(ApiOperation.class)))
						.and(RequestHandlerSelectors.withClassAnnotation(ApiIgnore.class).negate()))
//                .apis(Predicates.and(Predicates.or(RequestHandlerSelectors.withClassAnnotation(Api.class),
//                        RequestHandlerSelectors.withClassAnnotation(ApiModel.class),
//                        RequestHandlerSelectors.withMethodAnnotation(ApiOperation.class)),
//                        Predicates.not(RequestHandlerSelectors.withClassAnnotation(ApiIgnore.class))))
                .paths(PathSelectors.any())
                .build()
                .apiInfo(apiInfo());
    }

    /**
     * Creates an UiConfiguration bean that holds ui-related settings.
     *
     * @return The UiConfiguration bean
     */
    @Bean
    UiConfiguration uiConfig() {
        //Create bean
        return UiConfigurationBuilder.builder()
                .deepLinking(true)
                .displayOperationId(false)
                .defaultModelsExpandDepth(1)
                .defaultModelExpandDepth(1)
                .defaultModelRendering(ModelRendering.EXAMPLE)
                .displayRequestDuration(false)
                .docExpansion(DocExpansion.NONE)
                .filter(false)
                .maxDisplayedTags(null)
                .operationsSorter(OperationsSorter.METHOD)
                .showExtensions(false)
                .tagsSorter(TagsSorter.ALPHA)
                .supportedSubmitMethods(UiConfiguration.Constants.DEFAULT_SUBMIT_METHODS)
                .validatorUrl(null)
                .build();
    }

    /**
     * Creates an APiInfo object holding additional contact information that is supposed
     * to be displayed on the documentation ui.
     *
     * @return The APIInfo
     */
    private ApiInfo apiInfo() {
        return new ApiInfo(
                "Multi-purpose Binding and Provisioning Platform - REST API",
                "REST API documentation for the Multi-purpose Binding and Provisioning Platform",
                "1.0",
                "Terms of service",
                new Contact("IPVS", "https://www.ipvs.uni-stuttgart.de/abteilungen/as", "as@ipvs.uni-stuttgart.de"),
                "Apache License 2.0", "https://github.com/IPVS-AS/MBP/blob/master/LICENSE",
                Collections.emptyList());
    }
}
