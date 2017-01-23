package org.citopt.sensmonqtt.service;

import org.citopt.sensmonqtt.domain.device.Device;

public class ARPResult {
    
    private String ip;
    private String mac;

    public ARPResult(String ip, String mac) {
        this.ip = ip;
        this.mac = mac;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getMac() {
        return Device.formatMAC(mac);
    }

    public void setMac(String mac) {
        this.mac = Device.rawMAC(mac);
    }

    @Override
    public String toString() {
        return "ARPResult{" + "ip=" + ip + ", mac=" + mac + '}';
    }
    
}
