package de.ipvs.as.mbp.domain.discovery.device.monitoring;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class BlockedDevice {
    //MAC address of the blocked device
    private String macAddress;

    @JsonCreator
    public BlockedDevice(@JsonProperty("macAddress") String macAddress){
        setMacAddress(macAddress);
    }

    public String getMacAddress() {
        return macAddress;
    }

    public BlockedDevice setMacAddress(String macAddress) {
        this.macAddress = macAddress;
        return this;
    }

    //TODO reason...
}
