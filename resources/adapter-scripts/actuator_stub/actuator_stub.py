#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import sys
import paho.mqtt.client as mqtt
from datetime import datetime
import time
import os

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
        self.client.on_message = self._on_message

    def _on_connect(self, client, userdata, flags, rc):
        print("Connected with result code " + str(rc))

    def _on_message(self, client, userdata, message):
        # Convert message payload to string
        message_string = message.payload.decode(encoding='UTF-8')

        # Open actions log file and append message
        with open(ACTION_LOG_FILE, "a") as file:
            file.write(message_string)
            file.write("\n\n")

    def subscribe(self, topic):
        self.client.subscribe(topic)

    def start(self):
        # Start MQTT client
        self.client.connect(self.hostname, self.port, 60)
        self.client.loop_start()


def main(argv):
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

    # Keep script running
    while True:
        time.sleep(1)


# Call main function
if __name__ == "__main__":
    main(sys.argv[1:])
