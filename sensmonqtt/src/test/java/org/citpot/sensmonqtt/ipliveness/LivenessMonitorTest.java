/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.citpot.sensmonqtt.ipliveness;

import java.net.UnknownHostException;
import org.citopt.sensmonqtt.device.Device;
import org.citpot.sensmonqtt.ipliveness.LivenessMonitorTest.LivenessCallbackEmptyStub;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author rafaelkperes
 */
public class LivenessMonitorTest {
    
    private static String reachableIp = "8.8.8.8";
    private static String unreachableIp = "10.0.0.100";

    public LivenessMonitorTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }
    
    public class LivenessCallbackEmptyStub implements LivenessCallback {

        @Override
        public void onOnlineDevice(String ip) {
            System.out.println(ip + " is now REACHABLE!");
        }

        @Override
        public void onOfflineDevice(String ip) {
            System.out.println(ip + " is now UNREACHABLE!");
        }
        
    }

    /**
     * Test of run method on a Thread, of class LivenessMonitor.
     */
    @Test
    public void testRun() {
        System.out.println("run");

        Thread t = LivenessMonitor.getThread();
        
        boolean expResult = false;
        boolean result = LivenessMonitor.isRunning();
        assertEquals(expResult, result);

        t.start();
        expResult = true;
        result = LivenessMonitor.isRunning();
        assertEquals(expResult, result);
    }

    /**
     * Test of addIp method, of class LivenessMonitor.
     */
    @Test
    public void testAddIp() {
        System.out.println("addIp");
        String ip = reachableIp;
        LivenessMonitor instance = LivenessMonitor.getInstance();

        try {
            // check output - should be REACHABLE
            instance.addIp(ip, new LivenessCallbackEmptyStub());            
        } catch (UnknownHostException e) {
            fail("Got exception from " + ip);
        }
        
        ip = unreachableIp;
        try {
            // check output - should be UNREACHABLE
            instance.addIp(ip, new LivenessCallbackEmptyStub());            
        } catch (UnknownHostException e) {
            fail("Got exception from " + ip);
        }
        
        ip = "8.8.8.a";
        try {            
            instance.addIp(ip, new LivenessCallbackEmptyStub());
            fail("Should throw exception from " + ip);
        } catch (UnknownHostException e) {            
        }
    }

    /**
     * Test of getStatus method, of class LivenessMonitor.
     * @throws java.lang.Exception
     */
    @Test
    public void testGetStatus() throws Exception {
        System.out.println("getStatus");
        String ip = reachableIp;
        LivenessMonitor instance = LivenessMonitor.getInstance();
        Device.NetworkStatus expResult = Device.NetworkStatus.REACHABLE;
        Device.NetworkStatus result = instance.getStatus(ip);
        assertEquals(expResult, result);

        ip = unreachableIp;
        expResult = Device.NetworkStatus.UNREACHABLE;
        result = instance.getStatus(ip);
        assertEquals(expResult, result);
    }

    /**
     * Test of getInstance method, of class LivenessMonitor.
     */
    @Test
    public void testGetInstance() {
        System.out.println("getInstance");
        LivenessMonitor instance = LivenessMonitor.getInstance();
        assertEquals(LivenessMonitor.class, instance.getClass());
    }

}
