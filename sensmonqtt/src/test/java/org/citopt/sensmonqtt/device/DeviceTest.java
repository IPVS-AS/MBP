/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.citopt.sensmonqtt.device;

import java.util.ArrayList;
import java.util.List;
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
public class DeviceTest {

    private String macAddress;
    private List<Integer> pinSet;
    private String type;

    public DeviceTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
        macAddress = "AA:BB:CC:DD";
        type = "stub";
        pinSet = new ArrayList<>();
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of getType method, of class Device.
     */
    @Test
    public void testGetType() {
        System.out.println("getType");
        Device instance = new Device(macAddress, pinSet, type);
        String expResult = "stub";
        String result = instance.getType();
        assertEquals(expResult, result);
    }

    /**
     * Test of getMacAddress method, of class Device.
     */
    @Test
    public void testGetMacAddress() {
        System.out.println("getMacAddress");
        Device instance = new Device(macAddress, pinSet, type);
        String expResult = "AA:BB:CC:DD";
        String result = instance.getMacAddress();
        assertEquals(expResult, result);
    }

    /**
     * Test of getPinSet method, of class Device.
     */
    @Test
    public void testGetPinSet() {
        System.out.println("getPinSet");
        pinSet.add(1);
        Device instance = new Device(macAddress, pinSet, type);
        List<Integer> expResult = new ArrayList<>();
        expResult.add(1);
        List<Integer> result = instance.getPinSet();
        assertEquals(expResult, result);
        assertNotSame(pinSet, result); // should be create a new instance
                
        pinSet.add(2);
        expResult.add(2);
        Device anotherInstance = new Device(macAddress, pinSet, type);
        result = anotherInstance.getPinSet();
        assertEquals(expResult, result);        
        // tests if pinSet is accessible after Device is instanciated.
        // (it should create a new instance of the list for itself)
        result = instance.getPinSet();
        assertFalse(expResult.equals(result));
        System.out.println(result); // check if it contains only 1
    }

    /**
     * Test of getStatus method, of class Device.
     */
    @Test
    public void testGetStatus() {
        System.out.println("getStatus");
        Device instance = new Device(macAddress, pinSet, type);
        Device.Status expResult = Device.Status.INACTIVE;
        Device.Status result = instance.getStatus();
        assertEquals(expResult, result);

        instance = new Device(macAddress, pinSet, type, Device.Status.INACTIVE);
        expResult = Device.Status.INACTIVE;
        result = instance.getStatus();
        assertEquals(expResult, result);

        instance = new Device(macAddress, pinSet, type, Device.Status.ACTIVE);
        expResult = Device.Status.ACTIVE;
        result = instance.getStatus();
        assertEquals(expResult, result);
    }

    /**
     * Test of getNetStatus method, of class Device.
     */
    @Test
    public void testGetNetStatus() {
        System.out.println("getNetStatus");
        Device instance = new Device(macAddress, pinSet, type);
        Device.NetworkStatus expResult = Device.NetworkStatus.UNREACHABLE;
        Device.NetworkStatus result = instance.getNetStatus();
        assertEquals(expResult, result);

        instance = new Device(macAddress, pinSet, type, Device.Status.ACTIVE);
        expResult = Device.NetworkStatus.UNREACHABLE;
        result = instance.getNetStatus();
        assertEquals(expResult, result);
    }

    /**
     * Test of setStatus method, of class Device.
     */
    @Test
    public void testSetStatus() {
        System.out.println("setStatus");
        Device.Status status = Device.Status.ACTIVE;
        Device instance = new Device(macAddress, pinSet, type);
        instance.setStatus(status);
        assertEquals(instance.getStatus(), status);

        status = Device.Status.INACTIVE;
        instance.setStatus(status);
        assertEquals(instance.getStatus(), status);
        // should always be unreachable as set to INACTIVE
        assertEquals(instance.getNetStatus(), Device.NetworkStatus.UNREACHABLE);
    }

    /**
     * Test of setNetworkStatus method, of class Device.
     */
    @Test
    public void testSetNetworkStatus() {
        System.out.println("setNetworkStatus");
        Device.NetworkStatus netStatus = Device.NetworkStatus.REACHABLE;
        Device instance = new Device(macAddress, pinSet, type, Device.Status.INACTIVE);
        try {
            instance.setNetStatus(netStatus);
            fail("Should throw IllegalArgumentException.");
        } catch (IllegalArgumentException ex) {            
        }
        
        netStatus = Device.NetworkStatus.REACHABLE;
        instance = new Device(macAddress, pinSet, type, Device.Status.ACTIVE);
        instance.setNetStatus(netStatus);
        assertEquals(netStatus, instance.getNetStatus());        
    }

}
