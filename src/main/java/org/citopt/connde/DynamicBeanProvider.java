package org.citopt.connde;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;


/**
 * Allows to look up Spring managed beans by their class and to provide them to non-bean classes.
 */
@Component
public class DynamicBeanProvider implements ApplicationContextAware {
    private static ApplicationContext context;

    /**
     * Returns a certain Spring managed bean, given by its class.
     * Returns null if bean could not be found.
     *
     * @param beanClass The class of the bean
     * @return The corresponding bean
     */
    public static <T> T get(Class<T> beanClass) {
        return context.getBean(beanClass);
    }

    /**
     * Sets the internal application context.
     *
     * @param applicationContext The application context to set
     * @throws BeansException In case of an unexpected error
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
    }
}
