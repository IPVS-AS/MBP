/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.citopt.sensmonqtt.device;

import java.util.HashSet;
import java.util.Set;
import org.bson.types.ObjectId;
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
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of getId method, of class Device.
     */
    @Test
    public void testGetId() {
        System.out.println("getId");
        Device instance = new Device("AA:BB:CC:DD");
        ObjectId expResult = null;
        ObjectId result = instance.getId();
        assertEquals(expResult, result);
    }

    /**
     * Test of getMacAddress method, of class Device.
     */
    @Test
    public void testGetMacAddress() {
        System.out.println("getMacAddress");
        Device instance = new Device("AA:BB:CC:DD");
        String expResult = "aabbccdd";
        String result = instance.getMacAddress();
        assertEquals(expResult, result);
    }

    /**
     * Test of getAvailablePins method, of class Device.
     */
    @Test
    public void testGetAvailablePins() {
        System.out.println("getAvailablePins");
        Device instance = new Device();
        Set<Pin> expResult = new HashSet<>();
        Set<Pin> result = instance.getAvailablePins();
        assertEquals(expResult, result);
    }

    /**
     * Test of getAllocatedPins method, of class Device.
     */
    @Test
    public void testGetAllocatedPins() {
        System.out.println("getAllocatedPins");
        Device instance = new Device();
        Set<Pin> expResult = new HashSet<>();
        Set<Pin> result = instance.getAllocatedPins();
        assertEquals(expResult, result);
    }

    /**
     * Test of getLocation method, of class Device.
     */
    @Test
    public void testGetLocation() {
        System.out.println("getLocation");
        Device instance = new Device();
        Location expResult = null;
        Location result = instance.getLocation();
        assertEquals(expResult, result);
    }

    /**
     * Test of setLocation method, of class Device.
     */
    @Test
    public void testSetLocation() {
        System.out.println("setLocation");
        Location location = null;
        Device instance = new Device();
        instance.setLocation(location);
    }

    /**
     * Test of addPin method, of class Device.
     */
    @Test
    public void testAddPin() {
        System.out.println("addPin");
        Pin p = new Pin("D0", "0");
        Device instance = new Device();
        instance.addPin(p);
    }

    /**
     * Test of allocatePin method, of class Device.
     */
    @Test
    public void testAllocatePin() {
        System.out.println("allocatePin");
        try {
            Pin p = new Pin("D0", "0");
            Device instance = new Device();
            instance.allocatePin(p);
            fail("should throw excpetion");
        } catch(IllegalArgumentException e) {
            
        }
        
        System.out.println("allocatePin");
        try {
            Pin p = new Pin("D0", "0");
            Device instance = new Device();
            instance.addPin(p);
            instance.allocatePin(p);
        } catch(IllegalArgumentException e) {
            fail(e.toString());
        }
    }

    /**
     * Test of isAvailable method, of class Device.
     */
    @Test
    public void testIsAvailable() {
        System.out.println("isAvailable");
        Pin p = new Pin("D0", "0");
        Device instance = new Device("AA:BB:CC:DD");
        boolean expResult = false;
        boolean result = instance.isAvailable(p);
        assertEquals(expResult, result);
    }

    /**
     * Test of isAllocated method, of class Device.
     */
    @Test
    public void testIsAllocated() {
        System.out.println("isAllocated");
        Pin p = new Pin("D0", "0");
        Device instance = new Device();
        boolean expResult = false;
        boolean result = instance.isAllocated(p);
        assertEquals(expResult, result);
    }

    /**
     * Test of equals method, of class Device.
     */
    @Test
    public void testEquals() {
        System.out.println("equals");
        Object obj = null;
        Device instance = new Device("AA:BB:CC:DD");
        boolean expResult = false;
        boolean result = instance.equals(obj);
        assertEquals(expResult, result);
        
        obj = new Device();
        expResult = false;
        result = instance.equals(obj);
        assertEquals(expResult, result);
        
        obj = new Device("AA:BB:CC:DD");
        expResult = true;
        result = instance.equals(obj);
        assertEquals(expResult, result);
    }
    
}
