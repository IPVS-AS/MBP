package org.citopt.websensor.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class HeartbeatResult {
    
    public enum Status {
        UNDEFINED,
        REACHABLE,
        UNREACHABLE
    }
    
    private String ip;
    private String mac;
    private Status status;
    private Date date;

    public HeartbeatResult() {
    }

    public HeartbeatResult(String ip, String mac, Status status, Date date) {
        this.ip = ip;
        this.mac = mac;
        this.status = status;
        this.date = date;
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

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
    
    public void setStatus(String status) {
        this.status = Status.valueOf(status);
    }
    public Date getRawDate() {
        return date;
    }
    
    public String getDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(date);
    }

    public void setDate(Date date) {
        this.date = date;
    }
    
    public void setDate(String date) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS");
        this.date = sdf.parse(date);
    }

    @Override
    public String toString() {
        return "HeartbeatResult{" + "ip=" + ip + ", mac=" + mac + ", status=" + status + ", date=" + date + '}';
    }
    
}
