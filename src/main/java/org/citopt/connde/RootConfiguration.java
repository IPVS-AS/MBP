package org.citopt.connde;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Root configuration for the MBP app.
 */
@Configuration
@Import({MongoConfiguration.class})
public class RootConfiguration {}
