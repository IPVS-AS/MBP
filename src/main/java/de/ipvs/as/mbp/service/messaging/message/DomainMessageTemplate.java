package de.ipvs.as.mbp.service.messaging.message;

import de.ipvs.as.mbp.service.messaging.message.reply.ReplyMessageBody;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for classes that define the structure of publish-subscribe-based messages. It allows to specify
 * names for the various message types, as well as links to other message types that serve as replies
 * for requests.
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

    /**
     * If the message represents a request message for which reply messages are expected to receive,
     * this field will provide the class of which the bodies of the reply messages will be instances of.
     *
     * @return The body type of the reply messages
     */
    Class<? extends ReplyMessageBody> replyType() default ReplyMessageBody.NoReply.class;
}
