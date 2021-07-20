package de.ipvs.as.mbp.domain.discovery.deployment;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Enumeration of states in which {@link DynamicDeployment}s can be.
 */
public enum DynamicDeploymentState {
    DISABLED("disabled"), //Dynamic deployment is not set to active by the user
    IN_PROGRESS("in_progress"), //Operation in progress
    NO_CANDIDATE("no_candidate"),  //No candidate device found
    ALL_FAILED("all_failed"), // Candidate devices found, but deployment failed for all candidates
    DEPLOYED("deployed"); //Device found and successfully deployed

    //Externally visible name of the state
    private String name;

    /**
     * Creates a new dynamic deployment state with a given name.
     *
     * @param name The desired name of the state
     */
    DynamicDeploymentState(String name) {
        setName(name);
    }

    /**
     * Sets the name of the deployment state.
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
     * Serializes a dynamic deployment state by returning its name.
     *
     * @return The name of the state
     */
    @JsonValue
    public String value() {
        return this.name;
    }

    /**
     * Returns the dynamic deployment state that corresponds to a given name. This method is called when
     * a provided state name needs to be mapped to the actual state object.
     *
     * @param name The name of the dynamic deployment state
     * @return The corresponding state or null if not found
     */
    @JsonCreator
    public static DynamicDeploymentState create(String name) {
        //Sanity check for provided name
        if ((name == null) || name.isEmpty()) {
            return null;
        }

        //Compare every available state against the provided name
        for (DynamicDeploymentState state : values()) {
            if (name.equalsIgnoreCase(state.value())) {
                //Matching state found
                return state;
            }
        }

        //No matching state was found
        return null;
    }
}
