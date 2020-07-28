package org.citopt.connde;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.rest.webmvc.config.RepositoryRestMvcConfiguration;

/**
 * REST MVC configuration for the repositories used in the application.
 */
@Configuration
public class RestMVCConfiguration extends RepositoryRestMvcConfiguration {

	public RestMVCConfiguration(ApplicationContext context, ObjectFactory<ConversionService> conversionService) {
		super(context, conversionService);
	}
	
}