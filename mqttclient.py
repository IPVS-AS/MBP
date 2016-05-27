''' based on https://pypi.python.org/pypi/paho-mqtt/1.1#installation '''

import logging as log
import struct
from queue import Queue

import paho.mqtt.client as mqtt

from sensorbase import SensorStub

# sensorbase
sleeptime = 1
daemon = True
threads = []
sensorid = "stub"
queue_ = Queue()

url = "localhost"
port = 1883
topic = "public/me/"

# The callback for when the client receives a CONNACK response from the server.
def on_connect(client, userdata, flags, rc):
    log.debug("Connected with result code " + str(rc))

    # Subscribing in on_connect() means that if we lose the connection and
    # reconnect then subscriptions will be renewed.
    #client.subscribe("public/me/#")

# The callback for when a PUBLISH message is received from the server.
def on_message(client, userdata, msg):
    log.debug(msg.topic+" "+str(msg.payload))

# Called when a message that was to be sent using the publish() call has completed transmission to the broker.
def on_publish(client, userdata, mid):
    return

if __name__ == "__main__":
    stub = SensorStub(sensorid, queue_, sleeptime, daemon)

    client = mqtt.Client()
    client.on_connect = on_connect
    # client.on_message = on_message
    client.connect(url, port, 60)

    threads.append(stub)
    stub.start()

    while (True):
        value = queue_.get(block=True)
        client.publish(topic, payload=value["value"], qos=0, retain=False)

