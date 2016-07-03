package org.citpot.sensmonqtt.ipliveness;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.citopt.sensmonqtt.device.Device;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author rafaelkperes
 */
public class LivenessMonitor implements Runnable {

    private static LivenessMonitor SINGLETON_INSTANCE = null;
    private static Thread SINGLETON_THREAD = null;

    private final ConcurrentHashMap<InetAddress, LivenessCallback> ips;
    private final ConcurrentHashMap<InetAddress, Boolean> lastStatus;

    private static final int TIMEOUT = 50;
    
    public static Thread getThread() {
        if (SINGLETON_THREAD == null) {
            SINGLETON_THREAD = new Thread(LivenessMonitor.getInstance());
        }
        return SINGLETON_THREAD;
    }
    
    public static boolean isRunning() {
        return LivenessMonitor.getThread().isAlive();
    }

    @Override
    public void run() {
        try {
            while (true) {
                cicle();
            }
        } catch (IOException ex) {
            Logger.getLogger(LivenessMonitor.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("IOExcpetion in LivenessMonitor.");
        } finally {
        }
    }
    
    private void cicle() throws IOException {
        for (InetAddress addr : ips.keySet()) {
            Boolean wasReachable = lastStatus.get(addr);
            LivenessCallback callback = ips.get(addr);
            
            // if device goes online
            if (addr.isReachable(TIMEOUT)) {
                lastStatus.put(addr, true);
                if (wasReachable == null || !wasReachable) {
                        callback.onOnlineDevice(addr.getHostAddress());
                }
            } else {  // if device goes offline
                lastStatus.put(addr, false);
                if (wasReachable == null || wasReachable) {
                        callback.onOfflineDevice(addr.getHostAddress());
                }
            }
        }
    }

    /**
     * Add IP to the list, to it will be monitored by the thread. If it goes
     * offline, the callback given to the Monitor will be called.
     *
     * @param ip
     * @param callback which will be used on the thread when the IP changes state (online and offline)
     * @throws UnknownHostException
     */
    public void addIp(String ip, LivenessCallback callback) throws UnknownHostException {
        InetAddress addr = InetAddress.getByName(ip);
        this.ips.put(addr, callback);
    }

    /**
     * Use it to explicitly check for the IP status.
     *
     * @param ip
     * @return Status of the IP
     * @throws UnknownHostException
     */
    public Device.NetworkStatus getStatus(String ip) throws UnknownHostException, IOException {
        InetAddress addr = InetAddress.getByName(ip);
        if (addr.isReachable(TIMEOUT)) {
            return Device.NetworkStatus.REACHABLE;
        }
        return Device.NetworkStatus.UNREACHABLE;
    }

    public static LivenessMonitor getInstance() {
        if (SINGLETON_INSTANCE == null) {
            SINGLETON_INSTANCE = new LivenessMonitor();
        }
        return SINGLETON_INSTANCE;
    }

    private LivenessMonitor() {
        ips = new ConcurrentHashMap<>();
        lastStatus = new ConcurrentHashMap<>();
    }
}
