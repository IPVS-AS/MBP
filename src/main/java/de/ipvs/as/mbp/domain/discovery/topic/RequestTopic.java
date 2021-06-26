package de.ipvs.as.mbp.domain.discovery.topic;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.ipvs.as.mbp.domain.user_entity.MBPEntity;
import de.ipvs.as.mbp.domain.user_entity.UserEntity;
import de.ipvs.as.mbp.service.messaging.scatter_gather.RequestStageConfig;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.GeneratedValue;

/**
 * Objects of this class represent topics to which discovery requests can be sent. Each topic specifies a timeout
 * after which the sending of replies to requests can be considered as finished and a number of expected
 * replies after which the sending is considered as finished as well.
 */
@Document
@MBPEntity(createValidator = RequestTopicCreateValidator.class)
@ApiModel(description = "Model for discovery request topics")
public class RequestTopic extends UserEntity {
    private static final String TOPIC_PATTERN = "{userId}/discovery/{suffix}";

    @Id
    @GeneratedValue
    @ApiModelProperty(notes = "Request topic ID", example = "5c8f7ad66f9e3c1bacb0fa99", accessMode = ApiModelProperty.AccessMode.READ_ONLY, readOnly = true)
    private String id;

    @ApiModelProperty(notes = "Topic suffix", example = "manufacturing")
    private String suffix;

    @ApiModelProperty(notes = "Timeout value (in ms)", example = "5000")
    private int timeout; //Milliseconds, 10 ms - 1 minute

    @ApiModelProperty(notes = "Expected number of replies", example = "5")
    private int expectedReplies; // > 0


    /**
     * Creates a new empty request topic.
     */
    public RequestTopic() {

    }


    /**
     * Return the id of this entity.
     *
     * @return the id of this entity as {@code String}.
     */
    @Override
    public String getId() {
        return this.id;
    }

    /**
     * Sets the ID of the request topic.
     *
     * @param id The ID to set
     * @return The request topic
     */
    public RequestTopic setId(String id) {
        this.id = id;
        return this;
    }

    /**
     * Returns the suffix of the request topic.
     *
     * @return The suffix
     */
    public String getSuffix() {
        return suffix;
    }

    /**
     * Sets the suffix of the request topic.
     *
     * @param suffix The suffix to set
     * @return The request topic
     */
    public RequestTopic setSuffix(String suffix) {
        this.suffix = suffix;
        return this;
    }

    /**
     * Returns the timeout (in ms) after which the sending of replies to requests can be considered as finished.
     *
     * @return The timeout
     */
    public int getTimeout() {
        return timeout;
    }

    /**
     * Sets the timeout (in ms) after which the sending of replies to requests can be considered as finished.
     *
     * @param timeout The timeout value to set
     * @return The request topic
     */
    public RequestTopic setTimeout(int timeout) {
        this.timeout = timeout;
        return this;
    }

    /**
     * Returns the expected number of replies after which the sending of replies to requests can be considered as finished.
     *
     * @return The expected number of replies
     */
    public int getExpectedReplies() {
        return expectedReplies;
    }

    /**
     * Sets the expected number of replies after which the sending of replies to requests can be considered as finished.
     *
     * @param expectedReplies The expected number of replies to set
     * @return The request topic
     */
    public RequestTopic setExpectedReplies(int expectedReplies) {
        this.expectedReplies = expectedReplies;
        return this;
    }

    /**
     * Returns the full, absolute request topic stopic.
     *
     * @return The full topic string
     */
    @JsonProperty("fullTopic")
    public String getFullTopic() {
        //Fill in pattern
        return TOPIC_PATTERN
                .replaceAll("\\{userId}", this.getOwner().getId())
                .replaceAll("\\{suffix}", this.suffix);
    }
}
