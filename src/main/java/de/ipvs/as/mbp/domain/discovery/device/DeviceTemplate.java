package de.ipvs.as.mbp.domain.discovery.device;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.ipvs.as.mbp.domain.discovery.device.requirements.DeviceRequirement;
import de.ipvs.as.mbp.domain.discovery.device.requirements.RequirementsDeserializer;
import de.ipvs.as.mbp.domain.discovery.device.scoring.ScoringCriteriaDeserializer;
import de.ipvs.as.mbp.domain.discovery.device.scoring.ScoringCriterion;
import de.ipvs.as.mbp.domain.user_entity.MBPEntity;
import de.ipvs.as.mbp.domain.user_entity.UserEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.GeneratedValue;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Objects of this class represent device templates that describe requirements and criteria for the discovery of devices.
 */
@Document
@MBPEntity(createValidator = DeviceTemplateCreateValidator.class)
@ApiModel(description = "Model for device templates")
public class DeviceTemplate extends UserEntity {
    @Id
    @GeneratedValue
    @ApiModelProperty(notes = "Device template ID", example = "5c8f7ad66f9e3c1bacb0fa99", accessMode = ApiModelProperty.AccessMode.READ_ONLY, readOnly = true)
    private String id;

    @ApiModelProperty(notes = "Device template name", example = "My home", required = true)
    private String name;

    @ApiModelProperty(notes = "List of requirements for devices.")
    @JsonDeserialize(using = RequirementsDeserializer.class)
    private List<DeviceRequirement> requirements;

    @ApiModelProperty(notes = "List of scoring criteria for devices.")
    @JsonDeserialize(using = ScoringCriteriaDeserializer.class)
    private List<ScoringCriterion> scoringCriteria;


    /**
     * Creates a new device template.
     */
    public DeviceTemplate() {
        this.requirements = new ArrayList<>();
        this.scoringCriteria = new ArrayList<>();
    }

    /**
     * Adds a given requirement to the device template.
     *
     * @param requirement The requirement to add
     * @return The device template
     */
    public DeviceTemplate addRequirement(DeviceRequirement requirement) {
        //Sanity check
        if (requirement == null) {
            throw new IllegalArgumentException("Requirement must not be null.");
        }

        //Add requirement to list
        this.requirements.add(requirement);

        return this;
    }

    /**
     * Adds a given scoring criterion to the device template.
     *
     * @param scoringCriterion The scoring criterion to add
     * @return The device template
     */
    public DeviceTemplate addScoringCriterion(ScoringCriterion scoringCriterion) {
        //Sanity check
        if (scoringCriterion == null) {
            throw new IllegalArgumentException("Scoring criterion must not be null.");
        }

        //Add scoring criterion to list
        this.scoringCriteria.add(scoringCriterion);

        return this;
    }

    /**
     * Returns the ID of the device template.
     *
     * @return The ID
     */
    @Override
    public String getId() {
        return id;
    }

    /**
     * Sets the ID of the device template.
     *
     * @param id The ID to set
     * @return The device template
     */
    public DeviceTemplate setId(String id) {
        this.id = id;
        return this;
    }

    /**
     * Returns the name of the device template.
     *
     * @return The name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the device template.
     *
     * @param name The name to set
     * @return The device tempalte
     */
    public DeviceTemplate setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Returns the complete list of requirements that are associated with the device template.
     *
     * @return The list of requirements
     */
    public List<DeviceRequirement> getRequirements() {
        return requirements;
    }

    /**
     * Sets the list of requirements that are associated with the device template.
     *
     * @param requirements The list of requirements
     * @return The device template
     */
    public DeviceTemplate setRequirements(List<DeviceRequirement> requirements) {
        this.requirements = requirements;
        return this;
    }

    /**
     * Returns the complete list of scoring criteria that are associated with the device template.
     *
     * @return The list of scoring criteria
     */
    public List<ScoringCriterion> getScoringCriteria() {
        return scoringCriteria;
    }

    /**
     * Sets the list of scoring criteria that are associated with the device template.
     *
     * @param scoringCriteria The list of scoring criteria
     * @return The device template
     */
    public DeviceTemplate setScoringCriteria(List<ScoringCriterion> scoringCriteria) {
        this.scoringCriteria = scoringCriteria;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DeviceTemplate)) return false;
        if (id == null) return false;
        DeviceTemplate that = (DeviceTemplate) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
