package de.ipvs.as.mbp;

import de.ipvs.as.mbp.constants.Constants;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(exclude = SecurityAutoConfiguration.class, scanBasePackages = Constants.ROOT_PACKAGE)
public class MBPApplication {

    public static void main(String[] args) {
        SpringApplication.run(MBPApplication.class, args);
    }

}
