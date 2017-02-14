import sys
import logging
import datetime
from time import sleep

import json
import paho.mqtt.client as mqtt
import pymongo
from pymongo import MongoClient

import os

mongoclient = MongoClient()
db = mongoclient.sensmonqtt
coll = db['mqttlog']

device_topic = 'device/#'
sensor_topic = 'sensor/#'
url = 'localhost'
port = 1883

def on_message(client, userdata, msg):
    print (msg.payload)
    result = {
        'topic': str(msg.topic),
        'qos': str(msg.qos),
        'message': str(msg.payload),
        'date': str(datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S"))
    }
    jsonresult = json.dumps(result)

    try:
        parsed = json.loads(msg.payload.decode('utf-8'))
        result['id'] = parsed['id']
        result['value'] = str(parsed['value'])
    except Exception as e:
        result['id'] = 'NOT PARSEABLE'
        result['value'] = 'NOT PARSEABLE'
        print(e)
    coll.insert_one(result)
    

def on_connect(client, userdata, flags, rc):
    print("Connected with result code " + str(rc))

    # Subscribing in on_connect() means that if we lose the connection and
    # reconnect then subscriptions will be renewed.
    client.subscribe((sensor_topic, 0), (device_topic, 0))


if __name__ == "__main__":

    #setup MQTT connection
    mqttclient = mqtt.Client()
    mqttclient.connect(url, port, 60)
    mqttclient.on_connect = on_connect
    mqttclient.on_message = on_message
    mqttclient.loop_forever()