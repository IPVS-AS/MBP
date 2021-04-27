package de.ipvs.as.mbp.domain.settings;

import de.ipvs.as.mbp.util.Validation;

/**
 * Objects of this class hold the user-defined application-wide settings as models and may be used as DTOs when
 * communicating with clients.
 */
public class Settings {
    //All setting properties with default values
    private BrokerLocation brokerLocation = BrokerLocation.LOCAL;
    private String brokerIPAddress = "255.255.255.255";
    private boolean demoMode = false;

    /**
     * Creates a new settings object with default values.
     */
    public Settings() {

    }

    /**
     * Returns the location of the MQTT broker that is supposed to be used for the application.
     *
     * @return The broker location
     */
    public BrokerLocation getBrokerLocation() {
        return brokerLocation;
    }

    /**
     * Sets the broker location of the MQTT broker that is supposed to be used for the application.
     *
     * @param brokerLocation The broker location to set
     */
    public void setBrokerLocation(BrokerLocation brokerLocation) {
        //Sanity check
        if (brokerLocation == null) {
            throw new IllegalArgumentException("Broker location must not be null.");
        }
        this.brokerLocation = brokerLocation;
    }

    /**
     * Returns the IP address of the MQTT broker that is supposed to be used for the application. Only required if
     * the broker location is "remote".
     *
     * @return The IP address of the broker
     */
    public String getBrokerIPAddress() {
        return brokerIPAddress;
    }

    /**
     * Sets the IP address of the MQTT broker that is supposed to be used for the application. Only required if
     * the broker location is "remote".
     * @param brokerIPAddress The IP address of the broker to set
     */
    public void setBrokerIPAddress(String brokerIPAddress) {
        //Sanity check
        if ((brokerIPAddress == null) || brokerIPAddress.isEmpty()) {
            throw new IllegalArgumentException("Broker IP address must not be null.");
        }
        //Check if provided ip address is of a valid format
        if (!Validation.isValidIPAddress(brokerIPAddress)) {
            throw new IllegalArgumentException("Invalid broker IP address provided.");
        }

        this.brokerIPAddress = brokerIPAddress;
    }

    /**
     * Returns whether the demonstration mode is currently active.
     *
     * @return True, if the demonstration mode is active; false otherwise
     */
    public boolean isDemoMode() {
        return demoMode;
    }

    /**
     * Sets whether the demonstration mode is currently active.
     *
     * @param demoMode True, if the demonstration mode is active; false otherwise
     */
    public void setDemoMode(boolean demoMode) {
        this.demoMode = demoMode;
    }
}
