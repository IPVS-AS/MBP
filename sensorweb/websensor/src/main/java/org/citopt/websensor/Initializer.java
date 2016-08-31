package org.citopt.websensor;

import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

public class Initializer extends AbstractAnnotationConfigDispatcherServletInitializer {
    
    @Override
    protected Class<?>[] getRootConfigClasses() {
        System.out.println("root config");
        return new Class[] { RootConfiguration.class };
    }
  
    @Override
    protected Class<?>[] getServletConfigClasses() {
        System.out.println("servlet config");
        return new Class[] { WebConfiguration.class };
    }
  
    @Override
    protected String[] getServletMappings() {
        return new String[] { "/" };
    }
        
}
