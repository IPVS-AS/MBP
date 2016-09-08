#! /usr/bin/env python
import sys
import signal
import logging
import csv
import json
import datetime
from time import sleep

import paho.mqtt.client as mqtt
''' taken from http://stackoverflow.com/questions/27210396/python-scapy-arp-scanning-subnet-script-from-a-book '''
from scapy.all import srp
from scapy.all import Ether, ARP, conf
import pymongo
from pymongo import MongoClient

from repeatedtimer import RepeatedTimer

# logging setter
log = logging.getLogger(__name__)

def loginit(log, name):    
    #hdlr = logging.FileHandler(name + '.log')
    hdlr = logging.StreamHandler(sys.stdout)
    formatter = logging.Formatter('%(asctime)s %(levelname)s %(message)s')
    hdlr.setFormatter(formatter)
    log.addHandler(hdlr)
    log.setLevel(logging.DEBUG)

def arping(iprange="10.0.1.0/24"):
    """Arping function takes Address or Network, returns nested mac/ip list"""
    #conf, verb = 0
    ans, unans = srp(Ether(dst="ff:ff:ff:ff:ff:ff")/ARP(pdst=iprange), iface="en1", timeout=2)

    collection = []
    for snd, rcv in ans:
        result = rcv.sprintf(r"%ARP.psrc% %Ether.src%").split()
        collection.append(result)
    return collection

def generateCSV(filename, listOfTuples):
    log.info('generateCSV args:' + str(filename) + ',' + str(listOfTuples))

    log.info('opening file:' + str(filename) + '.csv')
    target = open(filename + '.csv', 'w')
    log.info('file opened:' + str(filename) + '.csv')

    log.info('started')

    try:
        csv_out = csv.writer(target, delimiter=',', lineterminator='\n')
        csv_out.writerow(['#IP_ADDR','#MAC_ADDR'])
        for row in listOfTuples:
            csv_out.writerow(row)

    except Exception as error:
        log.exception(__name__)

def repeatIt():
    #url = "localhost"
    #port = 1883
    #topic =  "arping/result"
    #log.info("connect:" + str(url) + ":" + str(port) + " " + str(60))

    #setup MQTT connection
    #client = mqtt.Client()
    #client.on_connect = on_connect
    #client.connect(url, port, 60)

    # call arping
    if len(sys.argv) > 1:
        for ip in sys.argv[1:]:
            log.info('arping:' + str(ip))
            result = arping(ip)
            log.info('arping result:' + str(result))
    # call arping with no args
    else:
        log.info('arping:noargs')
        result = arping()
        log.info('arping result:' + str(result))

    #result to MongoDB
    for ip, mac in result:
        print('entry:')
        print(ip)
        print(mac)

    # result to MQTT broker
    #dictresult = {'iptomac': result}
    #jsonresult = json.dumps(dictresult)
    #log.info('publish:' + jsonresult)
    #client.publish(topic, payload=str(jsonresult), qos=0, retain=False)

    # result to CSV file
    #log.info('generateCSV:' + str(result))
    #generateCSV('result', result)
    #log.info('generateCSV:returned')


# The callback for when the client receives a CONNACK response from the server.
def on_connect(client, userdata, flags, rc):    
    log.info("on_connect:" + str(rc))

# The callback for when a PUBLISH message is received from the server.
def on_message(client, userdata, msg):
    log.info("on_message:"+msg.topic+" "+str(msg.payload))

# Called when a message that was to be sent using the publish() call has completed transmission to the broker.
def on_publish(client, userdata, mid):
    log.info("on_publish")
    return

if __name__ == "__main__":    
    loginit(log, __name__)
    log.info('log setted')
    interval = 10

    try:
        client = MongoClient()
        db = client.sensmonqtt
        coll = db['arping']

        log.info('starting repeatIt with timer')
        rt = RepeatedTimer(interval, repeatIt)

        def signal_handler(signal, frame):
            rt.stop()
            sys.exit(0)
        signal.signal(signal.SIGINT, signal_handler)

        while True:
            sleep(interval * 100)

    except Exception as error:
        log.exception(__name__)
    finally:
        rt.stop()