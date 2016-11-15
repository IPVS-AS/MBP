package org.citopt.websensor;

import java.io.IOException;
import java.util.EnumSet;
import javax.servlet.DispatcherType;
import org.citopt.websensor.web.converter.DeviceConverter;
import org.citopt.websensor.web.converter.LocationConverter;
import org.citopt.websensor.web.converter.ScriptConverter;
import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.filter.HiddenHttpMethodFilter;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewResolverRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.handler.SimpleMappingExceptionResolver;
import org.springframework.web.servlet.view.tiles3.TilesConfigurer;
import org.springframework.web.servlet.view.tiles3.TilesViewResolver;

@Configuration
@ComponentScan({"org.citopt.websensor.web",
    "org.citopt.websensor.web.converter",
    "org.citopt.websensor.web.exception",
    "org.citopt.websensor.web.file"})
@EnableWebMvc
public class WebConfiguration extends WebMvcConfigurerAdapter {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new DeviceConverter());
        registry.addConverter(new LocationConverter());
        registry.addConverter(new ScriptConverter());
    }

    @Bean(name = "multipartResolver")
    public CommonsMultipartResolver getResolver() throws IOException {
        CommonsMultipartResolver resolver = new CommonsMultipartResolver();

        //Set the maximum allowed size (in bytes) for each individual file.
        resolver.setMaxUploadSizePerFile(5242880);//5MB

        //You may also set other available properties.
        return resolver;
    }

    @Bean
    public SimpleMappingExceptionResolver exceptionResolver() {
        SimpleMappingExceptionResolver simpleMappingExceptionResolver
                = new SimpleMappingExceptionResolver();
        simpleMappingExceptionResolver.addStatusCode("error/404", 404);

        return simpleMappingExceptionResolver;
    }

    @Bean
    public TilesConfigurer tilesConfigurer() {
        System.out.println("tilesConfigurer");
        TilesConfigurer tilesConfigurer = new TilesConfigurer();
        tilesConfigurer.setDefinitions(new String[]{"/WEB-INF/views/**/tiles.xml"});
        tilesConfigurer.setCheckRefresh(true);
        return tilesConfigurer;
    }

    @Override
    public void configureViewResolvers(ViewResolverRegistry registry) {
        System.out.println("configureViewResolvers");
        TilesViewResolver viewResolver = new TilesViewResolver();
        registry.viewResolver(viewResolver);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        System.out.println("addResourceHandlers");
        registry.addResourceHandler("/resources/**")
                .addResourceLocations("/resources/");
        registry.addResourceHandler("/static/**")
                .addResourceLocations("/static/");
    }
}
