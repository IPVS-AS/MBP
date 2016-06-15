/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.mqttonmaven;

import java.util.Iterator;
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
public class PahoDemo implements MqttCallback {

    private MqttClient client;

    public static void main(String[] args) {
        new PahoDemo().doDemo();
    }

    private void doDemo() {

        System.out.println("Starting demo...");

        String topic = "arping/result";
        String broker = "tcp://" + "localhost";
        String clientId = "java.mqttonmaven.PahoDemo";
        MemoryPersistence persistence = new MemoryPersistence();

        try {
            client = new MqttClient(broker, clientId, persistence);
            client.connect();
            client.setCallback(this);
            client.subscribe(topic);
        } catch (MqttException me) {
            System.out.println("reason " + me.getReasonCode());
            System.out.println("msg " + me.getMessage());
            System.out.println("loc " + me.getLocalizedMessage());
            System.out.println("cause " + me.getCause());
            System.out.println("excep " + me);
            me.printStackTrace();
        }
    }

    @Override
    public void connectionLost(Throwable thrwbl) {
    }

    @Override
    public void messageArrived(String string, MqttMessage mm) throws Exception {
        System.out.println("Message received: " + string);
        System.out.println("Content:" + mm.toString());

        JSONParser parser = new JSONParser();

        try {
            Object obj = parser.parse(mm.toString());
            JSONObject jobj = (JSONObject) obj;
            JSONArray array = (JSONArray) jobj.get("iptomac");
            
            Iterator it = array.iterator();
            while(it.hasNext()) {
                JSONArray row = (JSONArray) it.next();
                System.out.println(row.get(0) + " is " + row.get(1));
            }
        } catch (ParseException pe) {
            System.out.println("Invalid format.");
            pe.printStackTrace();
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken imdt) {
    }

}
