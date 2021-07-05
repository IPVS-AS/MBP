package de.ipvs.as.mbp.service.messaging.message;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for classes defining the body structure of the messages that are supposed to be transferred via
 * publish-subscribe-based messaging. It allows to specify an unique name for the message type that results
 * from using the body in a message and a topic suffix that is supposed to be appended to the topic under
 * which messages of this type are published.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface DomainMessageTemplate {

    /**
     * Provides the name of the message type that results from using the body in a message. This type name
     * allows to uniquely identify the type of messages.
     *
     * @return The type name
     */
    String value();

    /**
     * Provides the suffix that is supposed to be appended to the topics under which messages of this type
     * are published at the messaging broker.
     *
     * @return The topic suffix
     */
    String topicSuffix() default "";
}
