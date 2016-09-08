/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.citopt.sensmonqtt.device;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
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
public class SensorTest {

    private Device device;
    private List<Pin> pinSet;
    private Script type;
    private ObjectId id;

    public SensorTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
        id = new ObjectId();
        device = new Device("AA:BB:CC:DD");
        type = new Script();
        type.setId(id);
        pinSet = new LinkedList<>();
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
        Sensor instance = new Sensor(device, pinSet, type);
        Script expResult = new Script();
        expResult.setId(new ObjectId(id.toByteArray()));
        Script result = instance.getScript();
        assertEquals(expResult, result);
    }

    /**
     * Test of getMacAddress method, of class Device.
     */
    @Test
    public void testGetDevice() {
        System.out.println("getMacAddress");
        Sensor instance = new Sensor(device, pinSet, type);
        Device expResult = new Device("AA:BB:CC:DD");
        Device result = instance.getDevice();
        assertEquals(expResult, result);
    }

    /**
     * Test of getPinSet method, of class Device.
     */
    @Test
    public void testGetPinSet() {
        System.out.println("getPinSet");
        pinSet.add(new Pin("A0", "0"));
        Sensor instance = new Sensor(device, pinSet, type);
        List<Pin> expResult = new LinkedList<>();
        expResult.add(new Pin("A0", "0"));
        List<Pin> result = instance.getPinSet();
        assertEquals(expResult, result);

        pinSet.add(new Pin("A1", "1"));
        expResult = new LinkedList<>();
        expResult.add(new Pin("A1", "1"));
        expResult.add(new Pin("A0", "0"));
        Sensor anotherInstance = new Sensor(device, pinSet, type);
        result = anotherInstance.getPinSet();
        assertNotEquals(expResult, result);

        expResult = new LinkedList<>();
        expResult.add(new Pin("A0", "0"));
        expResult.add(new Pin("A1", "1"));
        anotherInstance = new Sensor(device, pinSet, type);
        result = anotherInstance.getPinSet();
        assertEquals(expResult, result);
    }

    /**
     * Test of getStatus method, of class Device.
     */
    @Test
    public void testGetStatus() {
        System.out.println("getStatus");
        Sensor instance = new Sensor(device, pinSet, type);
        Sensor.Status expResult = Sensor.Status.INACTIVE;
        Sensor.Status result = instance.getStatus();
        assertEquals(expResult, result);

        instance = new Sensor(device, pinSet, type, Sensor.Status.INACTIVE);
        expResult = Sensor.Status.INACTIVE;
        result = instance.getStatus();
        assertEquals(expResult, result);

        instance = new Sensor(device, pinSet, type, Sensor.Status.ACTIVE);
        expResult = Sensor.Status.ACTIVE;
        result = instance.getStatus();
        assertEquals(expResult, result);
    }

    /**
     * Test of getNetworkStatus method, of class Device.
     */
    @Test
    public void testGetNetStatus() {
        System.out.println("getNetworkStatus");
        Sensor instance = new Sensor(device, pinSet, type);
        Sensor.NetworkStatus expResult = Sensor.NetworkStatus.UNREACHABLE;
        Sensor.NetworkStatus result = instance.getNetworkStatus();
        assertEquals(expResult, result);

        instance = new Sensor(device, pinSet, type, Sensor.Status.ACTIVE);
        expResult = Sensor.NetworkStatus.UNREACHABLE;
        result = instance.getNetworkStatus();
        assertEquals(expResult, result);
    }

    /**
     * Test of setStatus method, of class Device.
     */
    @Test
    public void testSetStatus() {
        System.out.println("setStatus");
        Sensor.Status status = Sensor.Status.ACTIVE;
        Sensor instance = new Sensor(device, pinSet, type);
        instance.setStatus(status);
        assertEquals(instance.getStatus(), status);

        status = Sensor.Status.INACTIVE;
        instance.setStatus(status);
        assertEquals(instance.getStatus(), status);
        // should always be unreachable as set to INACTIVE
        assertEquals(instance.getNetworkStatus(), Sensor.NetworkStatus.UNREACHABLE);
    }

    /**
     * Test of setNetworkStatus method, of class Device.
     */
    @Test
    public void testSetNetworkStatus() {
        System.out.println("setNetworkStatus");
        Sensor.NetworkStatus netStatus = Sensor.NetworkStatus.REACHABLE;
        Sensor instance = new Sensor(device, pinSet, type, Sensor.Status.INACTIVE);
        try {
            instance.setNetworkStatus(netStatus);
            fail("Should throw IllegalArgumentException.");
        } catch (IllegalArgumentException ex) {
        }

        netStatus = Sensor.NetworkStatus.REACHABLE;
        instance = new Sensor(device, pinSet, type, Sensor.Status.ACTIVE);
        instance.setNetworkStatus(netStatus);
        assertEquals(netStatus, instance.getNetworkStatus());
    }
    
    @Test
    public void testEquals() {
        System.out.println("equals");
        Sensor result = new Sensor(device, pinSet, type);
        result.setStatus(Sensor.Status.ACTIVE);
        Sensor expResult = new Sensor(device, pinSet, type);
        assertEquals(expResult, result);
    }
}
