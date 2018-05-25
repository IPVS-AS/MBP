import os
import sys
import socket
import logging
import datetime
from time import sleep

import json
import paho.mqtt.client as mqtt
import pymongo
from pymongo import MongoClient

from repeatedtimer import RepeatedTimer


def arping_entry(coll, mac="", ip=""):
    if (mac):
        return coll.find_one({'mac': mac})
    elif (ip):
        return coll.find_one({'ip': ip})
    else:
        return coll.find_one()

def is_alive(ip):
    response = os.system("ping -n 2 -w 100 " + ip)
    if response == 0:
        return True
    else:
        return False
    '''try:
        socket.gethostbyaddr(ip)
        return True
    except socket.error as e:
        return False'''

def publish_result(_id, coll, client, topic, mac, ip, status):
    result = { 
        'ip': ip,
        'mac': mac,
        'status': status,
        'date': str(datetime.datetime.now().strftime("%H:%M:%S %d-%m-%Y"))
    }
    jsonresult = json.dumps(result)
    client.publish(topic, payload=str(jsonresult), qos=0, retain=False)
    coll.update({'_id': _id}, result, upsert=True)

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

    def step():
        print('step')
        registered_macs = mac_coll.find()
        for mac_entry in registered_macs:
            _id = mac_entry['_id']
            mac = str(mac_entry['mac'])
            arp_entry = arping_entry(coll=arp_coll, mac=mac)
            if (arp_entry): # if mac found in arping
                ip = arp_entry['ip']
                topic = device_topic + mac
                if (is_alive(ip)):
                    publish_result(_id, hb_coll, mqttclient, topic, mac, ip, 'REACHABLE')
                else:
                    publish_result(_id, hb_coll, mqttclient, topic, mac, ip, 'UNREACHABLE')

    while (True):
        interval = 10
        try:
            rt = RepeatedTimer(interval, step)
            while (True):
                sleep(interval * 100)
        except Exception as error:
            print (error)
            #log.exception(__name__)
        finally:
            rt.stop()
        
            