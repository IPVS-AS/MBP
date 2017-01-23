import sys
import logging
import datetime
import requests

import json
import paho.mqtt.client as mqtt

import os

CLIENT_URL = 'http://localhost:8080/sensmonqtt/api/valueLogs'

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
        parsed = json.loads(msg.payload.decode('utf-8'))
        result['idref'] = str(parsed['id'])
        result['value'] = str(parsed['value'])
    except Exception as e:
        result['idref'] = '-'
        result['value'] = '-'

    # do something with the data
    try:
        headers = {'content-type': 'application/json'}        
        r = requests.post(CLIENT_URL, data = json.dumps(result), headers = headers)
        print(r.status_code)
        print(str(r))
    except Exception as e:
        print ('error while doing POST')
        print (str(e))

def on_connect(client, userdata, flags, rc):
    print("Connected with result code " + str(rc))

    # Subscribing in on_connect() means that if we lose the connection and
    # reconnect then subscriptions will be renewed.
    client.subscribe([(SENSOR_TOPIC, 0),(ACTUATOR_TOPIC, 0),(DEVICE_TOPIC, 0)])


if __name__ == "__main__":
    print(requests.get(CLIENT_URL))

    #setup MQTT connection
    mqttclient = mqtt.Client()
    mqttclient.connect(MQTT_URL, MQTT_PORT, 60)
    mqttclient.on_connect = on_connect
    mqttclient.on_message = on_message
    mqttclient.loop_forever()