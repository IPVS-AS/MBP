#!/usr/bin/env python
# -*- coding: utf-8 -*-

from signal import *
import os, fnmatch
import sys, getopt
import json
from neopixel import *
from os.path import expanduser
from datetime import datetime
import time
import re

# --- Custom LIB imports
from mqttLIB import Client as mqttClient
from esp12e import ESP12e_Push
from ultrasonic import Ultrasonic


esp = None

def clean(*args):
    esp.turnOff()

def main(argv):

    for sig in (SIGABRT, SIGILL, SIGINT, SIGSEGV, SIGTERM):
        signal(sig, clean)

    # --- IP of esp
    esp_ip = "129.69.209.119" #placeholder
    broker_port = 1883
    #Read other measure interval from parameter data
    paramArray = json.loads(argv[0])
    for param in paramArray:
        if not ('name' in param and 'value' in param):
            continue
        elif param["name"] == "esp_ip":
            esp_ip = param["value"]
    
    configFileName = "connections.txt"
    topics = []
    brokerIps = []
    configExists = False

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

    # --- Create ESP12e instance
    global esp
    esp = ESP12e_Push(esp_ip, hostname, broker_port, topic_pub)
    esp.start()

    # --- Create Ultrasonic sensor instance
    ultrasonic = Ultrasonic(hostname, broker_port, topic)

    while ultrasonic.getLastValue() == None:
        ultrasonic.updateLastValue()
        time.sleep(1)


    while True:
        time.sleep(1)

if __name__ == "__main__":
   main(sys.argv[1:])
