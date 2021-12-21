package de.ipvs.as.mbp;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import de.ipvs.as.mbp.util.ValidationErrorCollection;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.lang.NonNull;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import com.fasterxml.classmate.TypeResolver;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.servlet.mvc.method.RequestMappingInfoHandlerMapping;
import springfox.documentation.annotations.ApiIgnore;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.data.rest.configuration.SpringDataRestConfiguration;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.spring.web.plugins.WebMvcRequestHandlerProvider;
import springfox.documentation.swagger.web.DocExpansion;
import springfox.documentation.swagger.web.ModelRendering;
import springfox.documentation.swagger.web.OperationsSorter;
import springfox.documentation.swagger.web.TagsSorter;
import springfox.documentation.swagger.web.UiConfiguration;
import springfox.documentation.swagger.web.UiConfigurationBuilder;
import springfox.documentation.swagger2.annotations.EnableSwagger2WebMvc;

/**
 * Configuration for the swagger documentation generator.
 */
@Import({SpringDataRestConfiguration.class})
@Configuration
@EnableSwagger2WebMvc
@EnableWebMvc
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

    /**
     * This is a workaround to get springfox swagger to work with spring boot 2.6.1.
     * See here for more information: https://github.com/springfox/springfox/issues/3462
     *
     * @return
     */
    @Bean
    public static BeanPostProcessor springfoxHandlerProviderBeanPostProcessor() {
        return new BeanPostProcessor() {

            @Override
            public Object postProcessAfterInitialization(@NonNull Object bean, @NonNull String beanName) throws BeansException {
                if (bean instanceof WebMvcRequestHandlerProvider) {
                    customizeSpringfoxHandlerMappings(getHandlerMappings(bean));
                }
                return bean;
            }

            private <T extends RequestMappingInfoHandlerMapping> void customizeSpringfoxHandlerMappings(List<T> mappings) {
                List<T> copy = mappings.stream()
                        .filter(mapping -> mapping.getPatternParser() == null)
                        .collect(Collectors.toList());
                mappings.clear();
                mappings.addAll(copy);
            }

            private List<RequestMappingInfoHandlerMapping> getHandlerMappings(Object bean) {
                try {
                    Field field = ReflectionUtils.findField(bean.getClass(), "handlerMappings");
                    field.setAccessible(true);
                    return (List<RequestMappingInfoHandlerMapping>) field.get(bean);
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    throw new IllegalStateException(e);
                }
            }
        };
    }
}
