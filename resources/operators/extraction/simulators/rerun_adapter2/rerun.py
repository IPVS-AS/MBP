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


class MQTTClient(object):

    def __init__(self, hostname, port, client_id):
        self.hostname = hostname
        self.port = port

        # Create MQTT client
        self.client = mqtt.Client(client_id=client_id, clean_session=True, userdata=None, protocol=mqtt.MQTTv31)

        # Register callback functions
        self.client.on_connect = self._on_connect

    def _on_connect(self, client, userdata, flags, rc):
        print("Connected with result code " + str(rc))

    def subscribe(self, topic):
        self.client.subscribe(topic)

    def start(self):
        # Start MQTT client
        self.client.connect(self.hostname, self.port, 60)
        self.client.loop_start()


def main(argv):
    print(argv[0])
    # Get path to connections file
    connections_file_path = os.path.join(os.getcwd(), CONNECTIONS_FILE)

    # Wait for connections file
    while not os.path.exists(connections_file_path):
        time.sleep(1)

    # Holds extracted topics/broker ips
    topics = []
    broker_ips = []

    # Parse connections file
    with open(connections_file_path, "r") as file:
        # Read connections file line by line
        for line in file.readlines():
            # Extract topic and ip address
            splits = line.split('=')
            ex_topic = splits[0].strip('\n').strip()
            ex_ip = splits[1].strip('\n').strip()

            # Update lists
            topics.append(ex_topic)
            broker_ips.append(ex_ip)

    # Choose hostname/publish topic for MQTT client
    hostname = broker_ips[0]
    topic_pub = topics[0]

    # Get id of the component that hosts this adapter
    component_id = topic_pub.split('/')[1]

    # Generate client id
    client_id = "id_%s" % (datetime.utcnow().strftime('%H_%M_%S'))

    # Generate action topic to subscribe to
    topic_sub = TOPIC_ACTION % component_id

    # Create and start MQTT client
    mqtt_client = MQTTClient(hostname, PORT, client_id)
    mqtt_client.start()
    mqtt_client.subscribe(topic_sub)

    # default interval for sending data
    sendingInterval = [30]
    sensorVals = []

    # Read other interval informations from parameter data
    paramArray = json.loads(argv[0])
    for param in paramArray:
        if not ('name' in param and 'value' in param):
            continue
        else:
            sendingInterval.append(param["name"])
            sensorVals.append(param["value"])

    try:
        # This loop ensures your code runs continuously,
        # for example, to read sensor values regularly at a given interval.
        while True:
            for sensorValue in sensorVals:
                value = sensorValue
                print(value)

                # waits a time interval before sending new data
            for sleepTime in sendingInterval:
                time.sleep(sleepTime)

            # send data to the MBP
            mbp.send_data(value)

    except:
        error = sys.exc_info()
        print('Error:', str(error))

# Call main function
if __name__ == "__main__":
    main(sys.argv[1:])
