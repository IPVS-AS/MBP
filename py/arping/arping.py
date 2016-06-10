#! /usr/bin/env python
import sys
import signal
import logging
import csv
import json
from time import sleep

import paho.mqtt.client as mqtt
''' taken from http://stackoverflow.com/questions/27210396/python-scapy-arp-scanning-subnet-script-from-a-book '''
from scapy.all import srp
from scapy.all import Ether, ARP, conf

from repeatedtimer import RepeatedTimer

# logging setter
logger = logging.getLogger(__name__)

# The callback for when the client receives a CONNACK response from the server.
def on_connect(client, userdata, flags, rc):    
    logger.info("on_connect:" + str(rc))

    # Subscribing in on_connect() means that if we lose the connection and
    # reconnect then subscriptions will be renewed.
    #client.subscribe("public/me/#")

# The callback for when a PUBLISH message is received from the server.
def on_message(client, userdata, msg):
    logger.info("on_message:"+msg.topic+" "+str(msg.payload))

# Called when a message that was to be sent using the publish() call has completed transmission to the broker.
def on_publish(client, userdata, mid):
    logger.info("on_publish")
    return

def loginit(logger, name):    
    hdlr = logging.FileHandler(name + '.log')
    formatter = logging.Formatter('%(asctime)s %(levelname)s %(message)s')
    hdlr.setFormatter(formatter)
    logger.addHandler(hdlr)
    logger.setLevel(logging.DEBUG)

def arping(iprange="10.0.1.0/24"):
    """Arping function takes IP Address or Network, returns nested mac/ip list"""

    #conf, verb = 0
    ans, unans = srp(Ether(dst="ff:ff:ff:ff:ff:ff")/ARP(pdst=iprange), timeout=2)

    collection = []
    for snd, rcv in ans:
        result = rcv.sprintf(r"%ARP.psrc% %Ether.src%").split()
        collection.append(result)
    return collection

def generateCSV(filename, listOfTuples):
    logger.info('generateCSV args:' + str(filename) + ',' + str(listOfTuples))

    logger.info('opening file:' + str(filename) + '.csv')
    target = open(filename + '.csv', 'w')
    logger.info('file opened:' + str(filename) + '.csv')

    logger.info('started')

    try:
        csv_out = csv.writer(target, delimiter=',', lineterminator='\n')
        csv_out.writerow(['#IP_ADDR','#MAC_ADDR'])
        for row in listOfTuples:
            csv_out.writerow(row)

    except Exception as error:
        logger.exception(__name__)

def repeatIt():
    url = "localhost"
    port = 1883
    topic =  "arping/result"
    logger.info("connect:" + str(url) + ":" + str(port) + " " + str(60))

    #setup MQTT connection
    client = mqtt.Client()
    client.on_connect = on_connect
    client.connect(url, port, 60)

    # call arping
    if len(sys.argv) > 1:
        for ip in sys.argv[1:]:
            logger.info('arping:' + str(ip))
            result = arping(ip)
            logger.info('arping result:' + str(result))
    # call arping with no args
    else:
        logger.info('arping:noargs')
        result = arping()
        logger.info('arping result:' + str(result))

    # result to MQTT broker
    logger.info('publish:' + json.dumps(result))
    client.publish(topic, payload=str(result), qos=0, retain=False)

    # result to CSV file
    logger.info('generateCSV:' + str(result))
    generateCSV('result', result)
    logger.info('generateCSV:returned')


if __name__ == "__main__":    
    loginit(logger, __name__)
    logger.info('logger setted')
    interval = 10  

    try:
        logger.info('starting repeatIt with timer')
        rt = RepeatedTimer(interval, repeatIt)

        def signal_handler(signal, frame):
            rt.stop()
            sys.exit(0)
        signal.signal(signal.SIGINT, signal_handler)

        while True:
            sleep(interval * 100)

    except Exception as error:
        logger.exception(__name__)
    finally:
        rt.stop()