package org.citopt.connde;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Configuration for operators that are supposed to be available as default operators.
 */
@Configuration
public class DefaultOperatorConfiguration {
    /**
     * Creates a bean representing a whitelist of paths to directories of operators that are supposed
     * to be available as default operators.
     *
     * @return The path whitelist bean
     */
    @Bean(name = "defaultOperatorWhitelist")
    public List<String> defaultOperatorWhitelist() {
        List<String> operatorPaths = Arrays.asList("/operators/extraction/temperature_stub");
        return Collections.unmodifiableList(operatorPaths);
    }
}
