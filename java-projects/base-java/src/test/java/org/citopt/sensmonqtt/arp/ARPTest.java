/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.citopt.sensmonqtt.arp;

import com.mongodb.DB;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import org.citopt.sensmonqtt.database.MongoUtils;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.simple.parser.ParseException;
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
public class ARPTest {

    private static DB db;

    public ARPTest() {
    }

    @BeforeClass
    public static void setUpClass() throws UnknownHostException {
        db = MongoUtils.getMongoDB(MongoUtils.getMongoClient());
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
     * Test of getIp method, of class ARP.
     */
    @Test
    public void testGetIp() throws UnknownHostException {
        System.out.println("getIp");
        String macAddress = "B8-86-87-D1-07-29";
        ARP instance = new ARP(db);
        String expResult = "10.0.0.1";
        String result = instance.getIp(macAddress);
        assertEquals(expResult, result);
    }

}
