package de.ipvs.as.mbp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * Main class, starting and running the MBP application.
 */
@SpringBootApplication
public class MBPApplication extends SpringBootServletInitializer {
    public static void main(String[] args) {
        SpringApplication.run(MBPApplication.class, args);
    }
}
