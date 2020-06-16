package org.citopt.connde;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.mongodb.MongoClient;
import org.citopt.connde.domain.adapter.Adapter;
import org.citopt.connde.repository.AdapterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.DispatcherServlet;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Root configuration for the app.
 */
@Configuration
@Import({MongoConfiguration.class})
public class RootConfiguration {

    /**
     * Creates a MongoDB client as bean.
     *
     * @return The bean
     */
    @Bean(name = "mongo")
    public MongoClient mongo() {
        System.out.println("load Mongo");
        return new MongoClient();
    }
}
