#!/usr/bin/env python
# -*- coding: utf-8 -*-
import sys, getopt
import paho.mqtt.client as mqtt
from datetime import datetime
import time
import json
import os, fnmatch
from os.path import expanduser

############################
# MQTT Client
############################
class mqttClient(object):
   hostname = 'localhost'
   port = 1883
   clientid = ''
   topic_sub = ''
   subscriber = False
   
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
      print(msg.topic + " " + str(msg.payload))
                        
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

   configFileName = "connections.txt"
   home = os.path.expandvars('$HOME/scripts')
   pattern = 'connde*'
   dirList = []
   topics = []
   brokerIps = []

   hostname = 'localhost'
   topic_pub = 'test'

   # walk through directory
   for root, dirs, files in os.walk(home):
       for dirName in dirs:
           # match search string
           if fnmatch.fnmatch(dirName, pattern):
               dirList.append(os.path.join(root, dirName))
               print (os.path.join(root, dirName))

   if (len(dirList) > 0):
       # check if configuration file exists
       configExists = False
       configFile = os.path.join(dirList[0], configFileName)

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
       
   else:
       print("Could not find root directory")
       sys.exit()

   hostname = brokerIps [0]
   topic_sub = topics [0]
   topic_splitted = topic_pub.split('/')
   component = topic_splitted [0]
   component_id = topic_splitted [1]
   
   print("Connecting to: " + hostname + " sub on topic: " + topic_sub)
   
   # --- Begin start mqtt client
   id = "id_%s" % (datetime.utcnow().strftime('%H_%M_%S'))
   subscriber = mqttClient(hostname, 1883, id)
   subscriber.startAsSubcriber(topic_sub)

   while (True):
      time.sleep(1)

if __name__ == "__main__":
   main(sys.argv[1:])
