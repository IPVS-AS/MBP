package org.citopt.websensor;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({MongoConfiguration.class})
@ComponentScan({"org.citopt.websensor.service"})
public class RootTestConfiguration {
    
}
