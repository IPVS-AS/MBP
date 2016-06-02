/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mqttpysensor.devicemonitor.arping;

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
 * @author rafae
 */
public class ArpingTest {

    private static Boolean usingStub;
    private static Arping arping;
    private Arping instance;

    public ArpingTest() {
    }

    @BeforeClass
    public static void setUpClass() {
        usingStub = true;
        arping = new Arping("10.0.0.*", usingStub);
    }

    @AfterClass
    public static void tearDownClass() {

    }

    @Before
    public void setUp() {
        instance = new Arping("10.0.0.*", usingStub);
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of getValues method, of class Arping.
     */
    @Test
    public void testGetValues() {
        System.out.println("getValues");

        //String expResult = "";
        Map result = instance.getValues();
        System.out.println(instance.getValues());

        if (ArpingTest.usingStub) {
            Map<String, String> expResult = new HashMap<>();
            expResult.put("b8:27:eb:91:aa:c3", "100.70.2.140");
            expResult.put("B8:86:87:D1:07:29", "100.70.2.139");
            expResult.put("b8:27:eb:c4:ff:96", "100.70.2.138");
            assertEquals(expResult, result);
        } else {
            System.out.println("");
            fail("Can't compare result in non-stub version. Check output.");
        }
    }

}
