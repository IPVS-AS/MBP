package org.citopt.connde.service.settings.model;

import org.citopt.connde.util.Validation;

/**
 * Created by Jan on 12.12.2018.
 */
public class Settings {

    //Setting fields with default values
    private BrokerLocation brokerLocation = BrokerLocation.LOCAL;
    private String brokerIPAddress = "255.255.255.255";

    public Settings(){

    }

    public BrokerLocation getBrokerLocation() {
        return brokerLocation;
    }

    public void setBrokerLocation(BrokerLocation brokerLocation) {
        if(brokerLocation == null){
            throw new IllegalArgumentException("Broker location must not be null.");
        }
        this.brokerLocation = brokerLocation;
    }

    public String getBrokerIPAddress() {
        return brokerIPAddress;
    }

    public void setBrokerIPAddress(String brokerIPAddress) {
        if((brokerIPAddress == null) || brokerIPAddress.isEmpty()){
            throw new IllegalArgumentException("Broker IP address must not be null.");
        }
        if(!Validation.isValidIPAddress(brokerIPAddress)){
            throw new IllegalArgumentException("Invalid broker IP address provided.");
        }

        this.brokerIPAddress = brokerIPAddress;
    }
}
