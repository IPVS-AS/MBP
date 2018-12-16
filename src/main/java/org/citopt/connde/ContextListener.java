package org.citopt.connde;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.io.IOException;

/**
 * Provides context listeners for the application that can be used to execute tasks that need to
 * be executed on startup and shutdown.
 *
 * @author Jan
 */
@WebListener
public class ContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        System.out.println("Startup");
        //TODO
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        System.out.println("Shutdown");
        //TODO
    }
}
