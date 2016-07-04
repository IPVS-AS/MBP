''' based on https://pypi.python.org/pypi/paho-mqtt/1.1#installation '''

import sys
import argparse
import logging
import struct
from queue import Queue

import paho.mqtt.client as mqtt

from sensorbase import SensorStub

# sensorbase
daemon = True
threads = []
queue_ = Queue()

log = logging.getLogger()
log.setLevel(logging.DEBUG)

ch = logging.StreamHandler(sys.stdout)
ch.setLevel(logging.DEBUG)
formatter = logging.Formatter('%(asctime)s - %(name)s - %(levelname)s - %(message)s')
ch.setFormatter(formatter)
log.addHandler(ch)

# The callback for when the client receives a CONNACK response from the server.
def on_connect(client, userdata, flags, rc):    
    log.info("on_connect:" + str(rc))

    # Subscribing in on_connect() means that if we lose the connection and
    # reconnect then subscriptions will be renewed.
    #client.subscribe("public/me/#")

# The callback for when a PUBLISH message is received from the server.
def on_message(client, userdata, msg):
    log.info("on_message:"+msg.topic+" "+str(msg.payload))

# Called when a message that was to be sent using the publish() call has completed transmission to the broker.
def on_publish(client, userdata, mid):
    log.info("on_publish")
    return

def _parse_args(argv):
    parser = argparse.ArgumentParser()

    sleeptime = 1
    url = "localhost"
    port = 1883
    
    parser.add_argument('-i','--sensor-id',help='Sensor ID to be logged by this instance.', required=True)
    parser.add_argument('-s','--sleeptime',help='Interval between each read from sensor in seconds (default: 1).', required=False)
    parser.add_argument('-u','--url',help='URL of the MQTT broker (default: "localhost").', required=False)
    parser.add_argument('-p','--port',help='Port of the MQTT broker (default: 1883).', required=False)
    parser.add_argument('-t','--topic',help='Port of the MQTT broker (default: sensor/$sensor-id).', required=False)

    args = parser.parse_args()
    args = vars(args)

    log.info("args:"+str(args))

    if ('sleeptime' not in args.keys() or args['sleeptime'] is None):
        args['sleeptime'] = sleeptime
        print(args['sleeptime'])
    if ('url' not in args.keys() or args['url'] is None):
        args['url'] = url
    if ('port' not in args.keys() or args['port'] is None):
        args['port'] = port
    if ('topic' not in args.keys() or args['topic'] is None):
        args['topic'] = 'sensor/' + args['sensor_id']

    return args

def main(argv):
    args = _parse_args(argv)
    sensorid = args['sensor_id']
    sleeptime = args['sleeptime']
    #print (args['url'])
    url = args['url']
    port = args['port']
    topic = args['topic']

    stub = SensorStub(sensorid, queue_, sleeptime, daemon)

    client = mqtt.Client()
    client.on_connect = on_connect
    # client.on_message = on_message

    log.info("connect:" + str(url) + ":" + str(port) + " " + str(60))
    client.connect(url, port, 60)

    threads.append(stub)
    stub.start()

    ''' add code here to send current ip + sensor id to the special topic '''

    while (True):
        value = queue_.get(block=True)
        log.info("publish:" + str(topic) + " " + str(value["value"]))
        client.publish(topic, payload=value["value"], qos=0, retain=False)

if __name__ == "__main__":
    main(sys.argv)