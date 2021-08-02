package de.ipvs.as.mbp.domain.discovery.device.scoring.capability;

import de.ipvs.as.mbp.domain.discovery.description.DeviceDescription;
import de.ipvs.as.mbp.domain.discovery.description.DeviceDescriptionCapability;
import de.ipvs.as.mbp.domain.discovery.device.scoring.ScoringCriterion;
import de.ipvs.as.mbp.error.EntityValidationException;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Abstract base class for capability-based scoring criteria.
 */
public abstract class CapabilityScoringCriterion implements ScoringCriterion {
    //Name of the capability
    protected String capabilityName;

    /**
     * Creates a new capability scoring criterion.
     */
    protected CapabilityScoringCriterion() {

    }

    /**
     * Creates a new scoring criterion for a capability by using the name of the pertaining capability.
     */
    public CapabilityScoringCriterion(String capabilityName) {
        //Set capability name
        setCapabilityName(capabilityName);
    }

    /**
     * Returns the name of the capability to which this criterion is supposed to be applied.
     *
     * @return The name of the capability
     */
    public String getCapabilityName() {
        return capabilityName;
    }

    /**
     * Sets the name of the capability to which this criterion is supposed to be applied.
     *
     * @param capabilityName The name of the capability to set
     * @return The capability scoring criterion
     */
    public CapabilityScoringCriterion setCapabilityName(String capabilityName) {
        this.capabilityName = capabilityName;
        return this;
    }

    /**
     * Validates the device scoring criterion by extending the provided exception with information about invalid fields.
     *
     * @param exception   The exception to extend as part of the validation
     * @param fieldPrefix Prefix that is supposed to be added to the fields that are validated
     */
    @Override
    public void validate(EntityValidationException exception, String fieldPrefix) {
        //Check capability name
        if ((this.capabilityName == null) || (this.capabilityName.isEmpty())) {
            exception.addInvalidField(fieldPrefix + ".capabilityName", "The capability name must not be empty.");
        }
    }

    /**
     * Tries to find a {@link DeviceDescriptionCapability} from a given {@link DeviceDescription} that matches the
     * capability name of this criterion and a given custom filter predicate.
     *
     * @param deviceDescription The device description that possibly contains the desired capability
     * @param filterPredicate   The custom filter predicate to use for beside the capability name to find the capability
     * @return An optional containing the found capability if available
     */
    protected Optional<DeviceDescriptionCapability> findCapability(DeviceDescription deviceDescription, Predicate<DeviceDescriptionCapability> filterPredicate) {
        //Null check
        if (deviceDescription == null) {
            return Optional.empty();
        }

        //Get capabilities from the device description
        List<DeviceDescriptionCapability> capabilities = deviceDescription.getCapabilities();

        //Null check
        if ((capabilities == null) || (capabilities.isEmpty()) ||
                (this.capabilityName == null) || (this.capabilityName.isEmpty())) {
            return Optional.empty();
        }

        //Try to find a capability that matches the name and provided filter predicate
        Stream<DeviceDescriptionCapability> stream = capabilities.stream()
                .filter(c -> this.capabilityName.equalsIgnoreCase(c.getName())) //Filter for capability name
                .filter(filterPredicate); //Filter for predicate

        //Get first element of stream
        return stream.findFirst();
    }
}
