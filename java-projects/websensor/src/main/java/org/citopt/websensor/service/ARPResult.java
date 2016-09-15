package org.citopt.websensor.service;

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
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    @Override
    public String toString() {
        return "ARPResult{" + "ip=" + ip + ", mac=" + mac + '}';
    }
    
}
