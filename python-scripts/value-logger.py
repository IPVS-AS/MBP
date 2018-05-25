import sys
import logging
import datetime

import json
import paho.mqtt.client as mqtt
import pymongo
from pymongo import MongoClient

import os

mongoclient = MongoClient()
db = mongoclient['connde']
coll = db['valueLog']

DEVICE_TOPIC = 'device/#'
SENSOR_TOPIC = 'sensor/#'
ACTUATOR_TOPIC = 'actuator/#'

MQTT_URL = 'localhost'
MQTT_PORT = 1883

def on_message(client, userdata, msg):
    print ('Received: ' + str(msg.payload))
    result = {
        'topic': str(msg.topic),
        'qos': str(msg.qos),
        'message': str(msg.payload),
        'date': str(datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S"))
    }

    try:
        print (msg.payload)
        parsed = json.loads(msg.payload.decode('utf-8'))
        print (parsed)
        print (result)
        result['idref'] = str(parsed["id"])
        print (result)
        result['value'] = str(parsed["value"])
        result['component'] = str(parsed["component"])
        if (result['component'] == "SENSOR"):
            result['sensorRef'] = {
                '_id': result['idref']
            }
        elif (result['component'] == "ACTUATOR"):
            result['actuatorRef'] = {
                '_id': result['idref']
            }

    except Exception as e:
        print (e)
        result['idref'] = '-'
        result['value'] = '-'
        result['component'] = '-'

    # do something with the data
    try:
        coll.insert_one(result)
    except Exception as e:
        print ('error while saving to db')
        print (str(e))

def on_connect(client, userdata, flags, rc):
    print("Connected with result code " + str(rc))

    # Subscribing in on_connect() means that if we lose the connection and
    # reconnect then subscriptions will be renewed.
    client.subscribe([(SENSOR_TOPIC, 0),(ACTUATOR_TOPIC, 0),(DEVICE_TOPIC, 0)])


if __name__ == "__main__":
    #setup MQTT connection
    mqttclient = mqtt.Client()
    mqttclient.connect(MQTT_URL, MQTT_PORT, 60)
    mqttclient.on_connect = on_connect
    mqttclient.on_message = on_message
    mqttclient.loop_forever()