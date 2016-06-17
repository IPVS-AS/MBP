/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.citopt.sensmonqtt.devicemanager.arping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author rafaelkperes
 */
public class ArpingSubscriber {

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

}
