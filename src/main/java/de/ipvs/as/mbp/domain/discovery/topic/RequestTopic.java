package de.ipvs.as.mbp.domain.discovery.topic;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.ipvs.as.mbp.domain.discovery.device.DeviceTemplateCreateValidator;
import de.ipvs.as.mbp.domain.discovery.device.requirements.RequirementsDeserializer;
import de.ipvs.as.mbp.domain.discovery.topic.condition.CompletenessCondition;
import de.ipvs.as.mbp.domain.discovery.topic.condition.CompletenessConditionDeserializer;
import de.ipvs.as.mbp.domain.user_entity.MBPEntity;
import de.ipvs.as.mbp.domain.user_entity.UserEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.GeneratedValue;

/**
 * Objects of this class represent topics to which discovery requests can be sent. Each topic specifies a completness
 * condition which indicates when the sending of replies to requests can be considered as finished.
 */
@Document
@MBPEntity(createValidator = DeviceTemplateCreateValidator.class)
@ApiModel(description = "Model for discovery request topics")
public class RequestTopic extends UserEntity {
    private static final String TOPIC_PATTERN = "{userId}/discovery/{suffix}";

    @Id
    @GeneratedValue
    @ApiModelProperty(notes = "Request topic ID", example = "5c8f7ad66f9e3c1bacb0fa99", accessMode = ApiModelProperty.AccessMode.READ_ONLY, readOnly = true)
    private String id;

    @ApiModelProperty(notes = "Topic suffix", example = "manufacturing")
    private String suffix;

    @JsonDeserialize(using = CompletenessConditionDeserializer.class)
    @ApiModelProperty(notes = "Completeness condition")
    private CompletenessCondition completenessCondition;


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
     * Returns the completeness condition of the request topic.
     *
     * @return The completeness condition
     */
    public CompletenessCondition getCompletenessCondition() {
        return completenessCondition;
    }

    /**
     * Sets the completeness condition of the request topic.
     *
     * @param completenessCondition The completeness condition to set
     * @return The request topic
     */
    public RequestTopic setCompletenessCondition(CompletenessCondition completenessCondition) {
        //Sanity check
        if (completenessCondition == null) {
            throw new IllegalArgumentException("Completeness condition must not be null.");
        }

        //Set completeness condition
        this.completenessCondition = completenessCondition;
        return this;
    }
}
