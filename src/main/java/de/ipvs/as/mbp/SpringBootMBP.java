package de.ipvs.as.mbp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@SpringBootApplication
@EnableWebMvc
@EnableSpringDataWebSupport
@EnableHypermediaSupport(type = EnableHypermediaSupport.HypermediaType.HAL)
public class SpringBootMBP {
    public static void main(String[] args) {
        SpringApplication.run(SpringBootMBP.class, args);
    }
}
