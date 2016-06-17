/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.citopt.sensmonqtt.devicemanager.arping;

import java.util.HashMap;
import java.util.Map;
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
public class ArpingSubscriberTest {
    
    private static String parseable;
    
    public ArpingSubscriberTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
        parseable = "{\"iptomac\": [[\"100.70.2.142\", \"b0:a8:6e:9a:8f:13\"], [\"100.70.2.140\", \"b8:27:eb:91:aa:c3\"]]}";
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
     * Test of parseJson method, of class ArpingSubscriber.
     * @throws java.lang.Exception
     */
    @Test
    public void testParseJson() throws Exception {
        System.out.println("parseJson");
        String toParse = parseable;
        ArpingSubscriber instance = new ArpingSubscriber();
        Map<String, String> expResult = new HashMap<>();
        expResult.put("b0:a8:6e:9a:8f:13", "100.70.2.142");
        expResult.put("b8:27:eb:91:aa:c3", "100.70.2.140");
        Map<String, String> result = instance.parseJson(toParse);
        assertEquals(expResult, result);
    }
    
}
