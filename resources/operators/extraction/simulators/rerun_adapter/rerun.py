#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import sys
import paho.mqtt.client as mqtt
from datetime import datetime
import time
import os
import json

########## Config ##########
PORT = 1883
CONNECTIONS_FILE = "connections.txt"
TOPIC_ACTION = "action/%s/#"
ACTION_LOG_FILE = "actions.txt"


############################


class mqttClient(object):
    hostname = 'localhost'
    port = 1883
    client_id = ''

    def __init__(self, hostname, port, client_id):
        self.hostname = hostname
        self.port = port
        self.clientid = client_id

        # Create MQTT client
        self.client = mqtt.Client(client_id=self.client_id, clean_session=True, userdata=None, protocol=mqtt.MQTTv31)

        # Register callback functions
        self.client.on_connect = self._on_connect

    def _on_connect(self, client, userdata, flags, rc):
        print("[" + datetime.utcnow().strftime('%Y-%m-%d %H:%M:%S.%f')[:-3] + "]: " + "ClientID: " + self.clientid + "; Connected with result code " + str(rc))
        print("Connected with result code " + str(rc))

    # publishes message to MQTT broker
    def sendMessage(self, topic, msg):
        self.client.publish(topic=topic, payload=msg, qos=0, retain=False)
        print(msg)

    def start(self):
        # Start MQTT client
        self.client.connect(self.hostname, self.port, 60)
        self.client.loop_start()


def main(argv):
    print(argv[0])

    # default interval for sending data
    sendingInterval = [30]
    sensorVals = []

    # Read other interval informations from parameter data
    paramArray = json.loads(argv[0])
    for param in paramArray:
        if not ('name' in param and 'value' in param):
            continue
        else:
            if(param["name"] == 'interval'):
                sendingInterval = param["value"]
            elif(param["name"] == 'value'):
                sensorVals = param["value"]


    configFileName = "connections.txt"
    topics = []
    brokerIps = []
    configExists = False

    hostname = 'localhost'
    topic_pub = 'test'

    configFile = os.path.join(os.getcwd(), configFileName)

    while (not configExists):
        configExists = os.path.exists(configFile)
        time.sleep(1)

    # BEGIN parsing file
    fileObject = open(configFile)
    fileLines = fileObject.readlines()
    fileObject.close()

    for line in fileLines:
        pars = line.split('=')
        topic = pars[0].strip('\n').strip()
        ip = pars[1].strip('\n').strip()
        topics.append(topic)
        brokerIps.append(ip)

    # END parsing file

    hostname = brokerIps [0]
    topic_pub = topics [0]
    topic_splitted = topic_pub.split('/')
    component = topic_splitted [0]
    component_id = topic_splitted [1]

    print("Connecting to: " + hostname + " pub on topic: " + topic_pub)

    # --- Begin start mqtt client
    id = "id_%s" % (datetime.utcnow().strftime('%H_%M_%S'))
    print(id)
    publisher = mqttClient(hostname, 1883, id)
    publisher.start()



    try:
        startTime = sendingInterval[0]
        counter = 1
        # This loop ensures your code runs continuously,
        # for example, to read sensor values regularly at a given interval.
        # while True:
        for sensorValue in sensorVals:
            value = sensorValue
            msg_pub = {"component": component.upper(), "id": component_id, "value": "%f" % (value) }
            publisher.sendMessage(topic_pub, json.dumps(msg_pub))
            print(topic_pub, json.dumps(msg_pub))
            if(counter < len(sendingInterval)):
                interval = sendingInterval[counter] - startTime
                startTime = sendingInterval[counter]
                counter += 1
                time.sleep(interval)



    except:
        error = sys.exc_info()
        print('Error:', str(error))

# Call main function
if __name__ == "__main__":
    main(sys.argv[1:])
