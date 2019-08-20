package org.citopt.connde;

import org.citopt.connde.service.cep.engine.core.CEPEngine;
import org.citopt.connde.service.cep.engine.esper.EsperCEPEngine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configures the CEP engine that is supposed to be used in this application and creates a corresponding bean.
 */
@Configuration
public class CEPConfiguration {

    /**
     * Creates a bean for the CEP engine to use.
     *
     * @return The CEP engine bean
     */
    @Bean(name = "cep_engine")
    public CEPEngine cepEngine() {
        System.out.println("load CEP Engine");
        return new EsperCEPEngine();
    }
}
