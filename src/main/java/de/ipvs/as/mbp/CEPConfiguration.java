package de.ipvs.as.mbp;

import de.ipvs.as.mbp.service.cep.engine.core.CEPEngine;
import de.ipvs.as.mbp.service.cep.engine.esper.EsperCEPEngine;
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
        return new EsperCEPEngine();
    }
}
