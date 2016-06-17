/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.citopt.sensmonqtt.devicemanager.arping;

import java.util.HashMap;
import java.util.Map;
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
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testParseJson() throws Exception {
        System.out.println("parseJson");
        String toParse = "{\"iptomac\": [[\"100.70.2.142\", \"b0:a8:6e:9a:8f:13\"], [\"100.70.2.140\", \"b8:27:eb:91:aa:c3\"]]}";
        ArpingSubscriber instance = new ArpingSubscriber();
        Map<String, String> expResult = new HashMap<>();
        expResult.put("b0:a8:6e:9a:8f:13", "100.70.2.142");
        expResult.put("b8:27:eb:91:aa:c3", "100.70.2.140");
        Map<String, String> result = instance.parseJson(toParse);
        assertEquals(expResult, result);
    }

    /**
     * Test of connectMqtt method, of class ArpingSubscriber.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testConnectMqtt() throws Exception {
        System.out.println("connectMqtt");
        ArpingSubscriber instance = new ArpingSubscriber();
        instance.connectMqtt();
        assertTrue(instance.isConnected());

        String toParse = "derp";
        MqttMessage mm = new MqttMessage(toParse.getBytes());
        try {
            instance.messageArrived("", mm);
            fail("Should throw Exception");
        } catch (ParseException ex) {
            // should stay connected after ParseException
            assertTrue(instance.isConnected());
        }
    }

    /**
     * Test of isConnected method, of class ArpingSubscriber.
     */
    @Test
    public void testIsConnected() {
        System.out.println("isConnected");
        ArpingSubscriber instance = new ArpingSubscriber();
        boolean expResult = false;
        boolean result = instance.isConnected();
        assertEquals(expResult, result);
    }

    /**
     * Test of getIp method, of class ArpingSubscriber.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testGetIp() throws Exception {
        System.out.println("getIp");
        String macAddress = "b0:a8:6e:9a:8f:13";
        ArpingSubscriber instance = new ArpingSubscriber();

        String toParse = "{\"iptomac\": [[\"100.70.2.142\", \"b0:a8:6e:9a:8f:13\"], [\"100.70.2.140\", \"b8:27:eb:91:aa:c3\"]]}";
        instance.messageArrived("topic", new MqttMessage(toParse.getBytes()));

        String expResult = "100.70.2.142";
        String result = instance.getIp(macAddress);
        assertEquals(expResult, result);
    }

    /**
     * Test of connectionLost method, of class ArpingSubscriber.
     */
    @Test
    public void testConnectionLost() {
        System.out.println("connectionLost");
        Throwable thrwbl = null;
        ArpingSubscriber instance = new ArpingSubscriber();
        instance.connectionLost(thrwbl);
    }

    /**
     * Test of messageArrived method, of class ArpingSubscriber.
     */
    @Test
    public void testMessageArrived() {
        System.out.println("messageArrived");
        String string = "";
        String toParse = "derp";
        MqttMessage mm = new MqttMessage(toParse.getBytes());
        ArpingSubscriber instance = new ArpingSubscriber();
        try {
            instance.messageArrived(string, mm);
            fail("Should throw Exception");
        } catch (ParseException ex) {
        }

        string = "";
        toParse = "{\"iptomac\": [[\"100.70.2.142\", \"b0:a8:6e:9a:8f:13\"], [\"100.70.2.140\", \"b8:27:eb:91:aa:c3\"]]}";
        mm = new MqttMessage(toParse.getBytes());
        instance = new ArpingSubscriber();
        try {
            instance.messageArrived(string, mm);
        } catch (ParseException ex) {
            fail(ex.toString());
        }
    }

    /**
     * Test of deliveryComplete method, of class ArpingSubscriber.
     */
    @Test
    public void testDeliveryComplete() {
        System.out.println("deliveryComplete");
        IMqttDeliveryToken imdt = null;
        ArpingSubscriber instance = new ArpingSubscriber();
        instance.deliveryComplete(imdt);
    }

}
