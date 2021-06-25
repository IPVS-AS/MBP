package de.ipvs.as.mbp.service.messaging.message;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for classes that define the structure of publish-subscribe-based messages. It allows to specify
 * a name for the corresponding message type by which the messages can be identified.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface DomainMessageTemplate {

    /**
     * Provides the domain-specific type name of the message which allows to identify the type
     * of the message.
     *
     * @return The domain-specific type name
     */
    String value();
}
