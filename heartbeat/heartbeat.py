import sys
import socket
import logging
import datetime
from time import sleep

import json
import paho.mqtt.client as mqtt
import pymongo
from pymongo import MongoClient

def arping_entry(coll, mac="", ip=""):
    if (mac):
        return coll.find_one({'mac': mac})
    elif (ip):
        return coll.find_one({'ip': ip})
    else:
        return coll.find_one()

def is_alive(ip):
    try:
        socket.gethostbyaddr(ip)
        return True
    except socket.error as e:
        return False

def publish_result(client, topic, mac, ip, status):
    result = { 
        'ip': ip,
        'mac': mac,
        'status': status,
        'date': str(datetime.datetime.utcnow())
    }
    jsonresult = json.dumps(result)
    client.publish(topic, payload=str(jsonresult), qos=0, retain=False)

if __name__ == "__main__":
    mongoclient = MongoClient()
    db = mongoclient.sensmonqtt
    arp_coll = db['arping']
    hb_coll = db['heartbeat']
    mac_coll = db['mac']

    #setup MQTT connection
    mqttclient = mqtt.Client()
    url = 'localhost'
    port = 1883
    mqttclient.connect(url, port, 60)
    device_topic = 'device/'

    while (True):
        registered_macs = mac_coll.find()
        for mac_entry in registered_macs:
            mac = str(mac_entry['mac'])
            print(mac)
            arp_entry = arping_entry(coll=arp_coll, mac=mac)
            if (arp_entry): # if mac found in arping
                ip = arp_entry['ip']
                topic = device_topic + mac
                if (is_alive(ip)):
                    print(topic)
                    publish_result(mqttclient, topic, mac, ip, 'REACHABLE')
                else:
                    print(topic)
                    publish_result(mqttclient, topic, mac, ip, 'UNREACHABLE')
            