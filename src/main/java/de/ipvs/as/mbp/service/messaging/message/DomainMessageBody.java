package de.ipvs.as.mbp.service.messaging.message;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Abstract base class for bodies of {@link DomainMessage}s. The body typically contains the actual information
 * of interest that is supposed to be transferred via the publish-subscribe-based messaging.
 */
public abstract class DomainMessageBody {
    /**
     * Retrieves the type name of the message body by inspecting the {@link DomainMessageTemplate} annotation that is
     * placed above the class of this message body. If no annotation is found, an exception will be thrown.
     *
     * @return The retrieved type name
     */
    @JsonIgnore
    public String getTypeName() {
        //Get class of the message body
        Class<? extends DomainMessageBody> messageBodyClass = this.getClass();

        //Check if annotation is present
        requireAnnotation();

        //Retrieve type name from the annotation
        return messageBodyClass.getAnnotation(DomainMessageTemplate.class).value();
    }

    /**
     * Retrieves the suffix that is supposed to be appended to the topics under which messages with this message
     * body are published. This happens by inspecting the {@link DomainMessageTemplate} annotation that is placed
     * above the class of this message body. If no annotation is found, an exception will be thrown.
     *
     * @return The retrieved topic suffix
     */
    @JsonIgnore
    public String getTopicSuffix() {
        //Get class of the message body
        Class<? extends DomainMessageBody> messageBodyClass = this.getClass();

        //Check if annotation is present
        requireAnnotation();

        //Retrieve topic suffix from the annotation
        return messageBodyClass.getAnnotation(DomainMessageTemplate.class).topicSuffix();
    }

    /**
     * Checks whether the {@link DomainMessageTemplate} annotation is placed above the class of this message body.
     * If this is not the case, an exception will be thrown.
     */
    private void requireAnnotation() {
        //Get class of message body
        Class<? extends DomainMessageBody> messageBodyClass = this.getClass();

        //Check if annotation is available
        if (!messageBodyClass.isAnnotationPresent(DomainMessageTemplate.class)) {
            throw new IllegalArgumentException(String.format("The class of the message body must be annotated with the %s annotation.", DomainMessageTemplate.class.getName()));
        }
    }
}
