#!/usr/bin/env python
# -*- coding: utf-8 -*-
import sys
import paho.mqtt.client as mqtt
from datetime import datetime
import time
import json
import os

# hardware imports
import RPi.GPIO as GPIO
relayPin = 14 # relay connected to D14
GPIO.setwarnings(False)
GPIO.setmode(GPIO.BCM)
GPIO.setup(relayPin, GPIO.OUT)

########## Config ##########
PORT = 1883
CONNECTIONS_FILE = "connections.txt"
TOPIC_ACTION = "action/%s/#"
ACTION_LOG_FILE = "actions.txt"


############################

############################
# MQTT Client
############################
class mqttClient(object):
   hostname = 'localhost'
   clientid = ''
   topic_sub = ''
   subscriber = False
   lastCmd = '-'
   
   def __init__(self, hostname, port, clientid):
      self.hostname = hostname
      self.port = port
      self.clientid = clientid
      
      # create MQTT client and set user name and password 
      self.client = mqtt.Client(client_id=self.clientid, clean_session=True, userdata=None, protocol=mqtt.MQTTv31)
      #client.username_pw_set(username="use-token-auth", password=mq_authtoken)

      # set mqtt client callbacks
      self.client.on_connect = self.on_connect
      self.client.on_message = self.on_message 

   # The callback for when the client receives a CONNACK response from the server.
   def on_connect(self, client, userdata, flags, rc):
      print("[" + datetime.utcnow().strftime('%Y-%m-%d %H:%M:%S.%f')[:-3] + "]: " + "ClientID: " + self.clientid + "; Connected with result code " + str(rc))
      if (self.subscriber):
         self.client.subscribe(self.topic_sub)

   # The callback for when a PUBLISH message is received from the server.
   def on_message(self, client, userdata, msg):
      payload_json = json.loads(msg.payload.decode('ascii'))
      if payload_json is not None:
         value_str = payload_json ["data"]
         value = float (value_str)
         
         if (value >= 1):
            if (self.lastCmd != 'ON'):
               GPIO.output(relayPin, GPIO.HIGH)
               self.lastCmd = 'ON'
               print('ON')

         else:
            if (self.lastCmd != 'OFF'):
               GPIO.output(relayPin, GPIO.LOW)
               self.lastCmd = 'OFF'
               print('OFF')

         # Convert message payload to string
         message_string = message.payload.decode(encoding='UTF-8')

         # Open actions log file and append message
         with open(ACTION_LOG_FILE, "a") as file:
            file.write(message_string)
            file.write("\n\n")
            
   # publishes message to MQTT broker
   def sendMessage(self, topic, msg):
      self.client.publish(topic=topic, payload=msg, qos=0, retain=False)
      print(msg)

   # connects to IBM IoT MQTT Broker
   def start(self):
      self.client.connect(self.hostname, self.port, 60)

      #runs a thread in the background to call loop() automatically.
      #This frees up the main thread for other work that may be blocking.
      #This call also handles reconnecting to the broker.
      #Call loop_stop() to stop the background thread.
      self.client.loop_start()

   # connects to IBM IoT MQTT Broker
   def startAsSubcriber(self, topic_sub):
      self.subscriber = True
      self.topic_sub = topic_sub
      self.start()
  
############################
# MAIN
############################
def main(argv):
   #default sleep interval
   measureInterval = 10
   topics = []
   brokerIps = []
   configExists = False
   
   hostname = 'localhost'
   topic_sub = 'test'

   configFile = os.path.join(os.getcwd(), CONNECTIONS_FILE)
   
   while (not configExists):
       configExists = os.path.exists(configFile)
       time.sleep(1)
       
   # BEGIN parsing file
   fileObject = open (configFile)
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
   topic_sub = topics [0]
   topic_splitted = topic_sub.split('/')
   component = topic_splitted [0]
   component_id = topic_splitted [1]
    
   # Generate action topic to subscribe to
   topic_sub = TOPIC_ACTION % component_id
   print("Connecting to: " + hostname + " sub on topic: " + topic_sub)
  
  
   # --- Begin start mqtt client
   id = "id_%s" % (datetime.utcnow().strftime('%H_%M_%S'))
   subscriber = mqttClient(hostname, PORT, id)
   subscriber.startAsSubcriber(topic_sub)

   while (True):
      time.sleep(measureInterval)
      
if __name__ == "__main__":
   main(sys.argv[1:])
