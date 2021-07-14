package de.ipvs.as.mbp.domain.discovery.peripheral;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Enumeration of states in which {@link DynamicPeripheral}s can be.
 */
public enum DynamicPeripheralStatus {
    DISABLED("disabled"), //Dynamic peripheral has not been enabled yet by the user
    DEPLOYING("deploying"), //Deployment for candidate devices in progress
    NO_CANDIDATE("no_candidate"),  //No candidate device found or deployment failed for all candidates
    RUNNING("running"); //Candidate found and deployed

    //Externally visible name of the state
    private String name;

    /**
     * Creates a new dynamic peripheral state with a given name.
     *
     * @param name The desired name of the state
     */
    DynamicPeripheralStatus(String name) {
        setName(name);
    }

    /**
     * Sets the name of the peripheral state.
     *
     * @param name The name to set
     */
    private void setName(String name) {
        //Sanity check
        if ((name == null) || name.isEmpty()) {
            throw new IllegalArgumentException("The name must not be null or empty.");
        }

        this.name = name;
    }

    /**
     * Serializes a dynamic peripheral state by returning its name.
     *
     * @return The name of the state
     */
    @JsonValue
    public String value() {
        return this.name;
    }

    /**
     * Returns the dynamic peripheral state that corresponds to a given name. This method is called when
     * a provided state name needs to be mapped to the actual state object.
     *
     * @param name The name of the dynamic peripheral state
     * @return The corresponding state or null if not found
     */
    @JsonCreator
    public static DynamicPeripheralStatus create(String name) {
        //Sanity check for provided name
        if ((name == null) || name.isEmpty()) {
            return null;
        }

        //Compare every available state against the provided name
        for (DynamicPeripheralStatus state : values()) {
            if (name.equalsIgnoreCase(state.value())) {
                //Matching state found
                return state;
            }
        }

        //No matching state was found
        return null;
    }
}
