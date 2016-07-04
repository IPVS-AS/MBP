/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.citopt.sensmonqtt.arp;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import org.citopt.sensmonqtt.constant.GlobalValues;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author rafaelkperes
 */
public class ARP implements MqttCallback {

    private final Map<String, String> mac2Ip;
    private MqttClient client;

    private static ARP SINGLETON_INSTANCE = null;
    private static final String ARPING_TOPIC = "arping/result";
    private static final String ARPING_CLIENT_ID
            = "org.citopt.sensmonqtt.devicemanager.arping";
    
    public static ARP getInstance() {
        if (SINGLETON_INSTANCE == null) {
            SINGLETON_INSTANCE = new ARP();
        }
        return SINGLETON_INSTANCE;
    }
        
    private ARP() {
        this.mac2Ip = new HashMap<>();
        this.client = null;
    }

    public String getIp(String macAddress) throws NoSuchElementException {
        String ip = mac2Ip.get(macAddress);
        if (ip != null) {
            return ip;
        }
        throw new NoSuchElementException();
    }

    public void connectMqtt() throws MqttException {
        MemoryPersistence persistence = new MemoryPersistence();
        this.client = new MqttClient(GlobalValues.MQTT_BROKER_URL, 
                ARPING_CLIENT_ID,
                persistence);
        client.connect();
        client.setCallback(this);
        client.subscribe(ARPING_TOPIC);
    }

    public boolean isConnected() {
        return this.client != null && client.isConnected();
    }

    public Map<String, String> parseJson(String toParse) throws ParseException {
        Map<String, String> parsed = new HashMap<>();

        JSONParser parser = new JSONParser();

        Object obj = parser.parse(toParse);
        JSONObject jobj = (JSONObject) obj;
        JSONArray array = (JSONArray) jobj.get("iptomac");

        Iterator it = array.iterator();
        while (it.hasNext()) {
            JSONArray row = (JSONArray) it.next();
            if (row.size() >= 2) {
                String ip = row.get(0).toString();
                String mac = row.get(1).toString();
                parsed.put(mac, ip);
            }
        }

        return parsed;
    }

    @Override
    public void connectionLost(Throwable thrwbl) {
    }

    /**
     * Will add any new entries received and replace old ones.
     *
     * @param string
     * @param mm
     * @throws org.json.simple.parser.ParseException
     */
    @Override
    public void messageArrived(String string, MqttMessage mm) throws ParseException {
        Map<String, String> newEntries = parseJson(mm.toString());
        this.mac2Ip.putAll(newEntries);
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken imdt) {
    }

}
