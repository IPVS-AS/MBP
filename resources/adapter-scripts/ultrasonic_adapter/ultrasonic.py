#!/usr/bin/env python
# -*- coding: utf-8 -*-

import time

# --- Lib for MQTT
from mqttLIB import Client as mqttClient

# --- Lib for utility functions
import utils as utils

class Ultrasonic(object):
    created_at = None
    last_value_time = None
    last_value = None

    mqttClient = None
    id = None

    def __init__(self, broker_ip, broker_port, topic):
        self.created_at = utils.getNowTime()

        self.id = utils.createNewId()

        # --- Create and initialize MQTT client
        self.mqttClient = mqttClient(broker_ip, broker_port, self.id)
        self.mqttClient.connect()
        self.mqttClient.subscribe(topic)
        self.mqttClient.start()

    def updateLastValue(self):
        lastMessage = self.mqttClient.getLastMessage()
        if(not (lastMessage == None)):
            value = lastMessage['value']
            self.setLastValue(value)

    def setLastValue(self, value):
        self.last_value = value
        self.last_value_time = utils.getNowTime()

    def getLastValue(self):
        return self.last_value

    def getLastValueTime(self):
        return self.last_value_time

    