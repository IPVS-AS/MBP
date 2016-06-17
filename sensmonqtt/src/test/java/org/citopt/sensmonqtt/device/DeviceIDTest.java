/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.citopt.sensmonqtt.device;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
public class DeviceIDTest {
    
    private String macAddress;
    private List<Integer> pinSet;
    
    public DeviceIDTest() {
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
        pinSet = new ArrayList<>();
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of getId method, of class DeviceID.
     */
    @Test
    public void testGetId() {
        System.out.println("getId");
        pinSet.add(1);
        DeviceID instance = new DeviceID(macAddress, pinSet);
        String expResult = "AA:BB:CC:DD,1";
        String result = instance.getId();
        assertEquals(expResult, result);
        
        pinSet.add(2);
        instance = new DeviceID(macAddress, pinSet);
        expResult = "AA:BB:CC:DD,1,2";
        result = instance.getId();
        assertEquals(expResult, result);
    }

    /**
     * Test of toString method, of class DeviceID.
     */
    @Test
    public void testToString() {
        System.out.println("toString");
        pinSet.add(1);
        pinSet.add(2);
        DeviceID instance = new DeviceID(macAddress, pinSet);
        String expResult = "AA:BB:CC:DD,1,2";
        String result = instance.toString();
        assertEquals(expResult, result);
    }

    /**
     * Test of hashCode method, of class DeviceID.
     */
    @Test
    public void testHashCode() {
        System.out.println("hashCode");
        pinSet.add(1);
        DeviceID instance = new DeviceID(macAddress, pinSet);
        int expResult = 47 * 5 + Objects.hashCode("AA:BB:CC:DD,1");
        int result = instance.hashCode();
        assertEquals(expResult, result);
    }

    /**
     * Test of equals method, of class DeviceID.
     */
    @Test
    public void testEquals() {
        System.out.println("equals");
        Object obj = null;
        pinSet.add(2);
        DeviceID instance = new DeviceID(macAddress, pinSet);
        boolean expResult = false;
        boolean result = instance.equals(obj);
        assertEquals(expResult, result);
        
        obj = new Object();
        instance = new DeviceID(macAddress, pinSet);
        expResult = false;
        result = instance.equals(obj);
        assertEquals(expResult, result);
        
        // same pinSet
        pinSet.add(1);
        obj = new DeviceID("A:B:C:D", pinSet);
        instance = new DeviceID(macAddress, pinSet);
        expResult = false;
        result = instance.equals(obj);
        assertEquals(expResult, result);
        
        // same mac
        pinSet.add(1);
        obj = new DeviceID(macAddress, pinSet);
        pinSet = new ArrayList<>(pinSet);
        pinSet.add(2);
        instance = new DeviceID(macAddress, pinSet);
        expResult = false;
        result = instance.equals(obj);
        assertEquals(expResult, result);
        
        // same mac & pinSet - should be equal
        pinSet.add(1);
        obj = new DeviceID(macAddress, pinSet);
        instance = new DeviceID(macAddress, pinSet);
        expResult = true;
        result = instance.equals(obj);
        assertEquals(expResult, result);
        
        // should ignore case
        pinSet.add(1);
        obj = new DeviceID("ABCD", pinSet);
        instance = new DeviceID("abcd", pinSet);
        expResult = true;
        result = instance.equals(obj);
        assertEquals(expResult, result);
        
        // should ignore order of pins ???
    }
    
}