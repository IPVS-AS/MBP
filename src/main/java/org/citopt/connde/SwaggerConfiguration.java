package org.citopt.connde;

import com.fasterxml.classmate.TypeResolver;
import com.google.common.base.Predicates;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import org.citopt.connde.util.ValidationErrorCollection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.data.rest.configuration.SpringDataRestConfiguration;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.web.*;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Collections;

/**
 * Configuration for the swagger documentation generator.
 */
@Import({SpringDataRestConfiguration.class})
@Configuration
@EnableSwagger2
public class SwaggerConfiguration {
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
                .apis(Predicates.or(RequestHandlerSelectors.withClassAnnotation(Api.class),
                        RequestHandlerSelectors.withClassAnnotation(ApiModel.class)))
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
