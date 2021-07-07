package de.ipvs.as.mbp.domain.discovery.description;

/**
 * DTO for {@link DeviceDescription}s that adds an additional score field.
 */
public class DeviceDescriptionDTO extends DeviceDescription {

    //Score of the device description
    private int score;

    /**
     * Creates a new DTO from a given device description and the score value that is associated with it.
     *
     * @param deviceDescription The device description to create the DTO from
     * @param score             The score value that is associated with the device description
     */
    public DeviceDescriptionDTO(DeviceDescription deviceDescription, int score) {
        super();

        //Sanity check
        if (deviceDescription == null) {
            throw new IllegalArgumentException("The device description must not be null.");
        }

        //Copy fields from the given device description
        this.setName(deviceDescription.getName());
        this.setDescription(deviceDescription.getDescription());
        this.setKeywords(deviceDescription.getKeywords());
        this.setLocation(deviceDescription.getLocation());
        this.setIdentifiers(deviceDescription.getIdentifiers());
        this.setCapabilities(deviceDescription.getCapabilities());
        this.setAttachments(deviceDescription.getAttachments());
        this.setSshDetails(deviceDescription.getSshDetails());
        this.setLastUpdateTimestamp(deviceDescription.getLastUpdateTimestamp());

        //Set score
        setScore(score);
    }

    /**
     * Returns the score that is associated with the device description.
     *
     * @return The score
     */
    public int getScore() {
        return score;
    }

    /**
     * Sets the score that is associated with the device description.
     *
     * @param score The score to set
     * @return The device description DTO
     */
    public DeviceDescriptionDTO setScore(int score) {
        //Sanity check
        if (score < 0) {
            throw new IllegalArgumentException("The score must be greater than or equal to zero.");
        }

        //Set score
        this.score = score;
        return this;
    }
}
