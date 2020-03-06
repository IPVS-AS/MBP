#!/usr/bin/env python
# -*- coding: utf-8 -*-

import time
import requests 
from signal import *

from httpLIB import Http
import utils as utils

class ESP12e_Push(object):
    created_at = None

    # --- MQTT config
    broker_ip = None
    broker_port = None
    topic = None

    # --- HTTP handler
    httpClient = None

    # --- Placeholder
    esp_ip = "10.42.0.156"

    def __init__(self, esp_ip, broker_ip, broker_port, topic):
        self.created_at = utils.getNowTime()

        self.updateConfig(esp_ip, broker_ip, broker_port, topic)

        self.httpClient = Http()

    def updateConfig(self, esp_ip, broker_ip, broker_port, topic):
        self.esp_ip = esp_ip
        self.broker_ip = broker_ip
        self.broker_port = broker_port
        self.topic = topic
        

    def sendConfig(self):
        #data = '{"ip": "'+self.broker_ip+'", "topic": "'+self.topic+'"}"'
        # --- Model for messaging directly with MBP
        topic_splitted = self.topic.split('/')
        component = topic_splitted [0]
        component_id = topic_splitted [1]

        #component = 'sensor'
        #component_id = "5ddcf4726d7b9c0dc6b5b5ed"
        status = '0'
        data = '{"ip": "'+self.broker_ip+'", "topic": "'+self.topic+'","component": "'+component+'","componentId": "'+component_id+'", "status": "'+status+'"}'

        self.httpClient.sendRequest(self.esp_ip, 80, "config", data)

    def sendStatus(self, status):
        data = "{'status': '"+str(status)+"'}"
        self.httpClient.sendRequest(self.esp_ip, 80, "status", data)

    def start(self):
        self.sendConfig()
        self.turnOn()

    def turnOff(self):
        self.sendStatus(0)

    def turnOn(self):
        self.sendStatus(1)

    def clean(*args):
        self.turnOff()
        data = "{'status': '"+str(0)+"'}"
        #sendConfig(esp_ip, "localhost", "sensor/123412341234123412341234", "sensor", "123412341234123412341234", "0")
        sys.exit(0)

    

    