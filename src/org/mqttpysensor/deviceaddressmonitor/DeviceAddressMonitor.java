/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mqttpysensor.deviceaddressmonitor;

import java.util.HashMap;
import java.util.Map;
import org.mqttpysensor.deviceaddressmonitor.arping.Arping;

/**
 *
 * @author rafae
 */
public class DeviceAddressMonitor {

    private Map<String, String> devices;
    private Arping arping;
    
    public DeviceAddressMonitor() {
        devices = new HashMap<>();
    }
    
    
}
