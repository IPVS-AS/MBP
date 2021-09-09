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

   lastValue = 0
   sensor = ""
   topic = ""
   axis = ""

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

   def setSensor(self, sensor):
      self.sensor = sensor

   def setTopic(self, id):
      self.topic = "XDK/" + id

   def setAxis(self, axis):
      self.axis = axis

   def getSensor(self):
      return self.sensor

   def setLastValue(self, value):
      self.lastValue = value

   def getLastValue(self):
      return self.lastValue

   # The callback for when the client receives a CONNACK response from the server.
   def on_connect(self, client, userdata, flags, rc):
      print("[" + datetime.utcnow().strftime('%Y-%m-%d %H:%M:%S.%f')[:-3] + "]: " + "ClientID: " + self.clientid + "; Connected with result code " + str(rc))

   def on_message(self, client, userdata, message):
      print("message received")
      parsed_json = json.loads(message.payload.decode("utf-8", "ignore"))
      if self.axis == "":
         self.setLastValue(parsed_json[self.sensor])
      else:
         self.setLastValue(parsed_json[self.sensor][self.axis])

   # publishes message to MQTT broker
   def sendMessage(self, topic, msg):
      self.client.publish(topic=topic, payload=msg, qos=0, retain=False)
      print(msg)

   # connects to MQTT Broker
   def startPublisher(self):
      self.client.connect(self.hostname, self.port, 60)

      #runs a thread in the background to call loop() automatically.
      #This frees up the main thread for other work that may be blocking.
      #This call also handles reconnecting to the broker.
      #Call loop_stop() to stop the background thread.
      self.client.loop_start()

   def startSubscriber(self):
      self.client.connect(self.hostname, self.port, 60)
      self.client.subscribe(self.topic)

      #runs a thread in the background to call loop() automatically.
      #This frees up the main thread for other work that may be blocking.
      #This call also handles reconnecting to the broker.
      #Call loop_stop() to stop the background thread.
      self.client.loop_start()

############################
# MAIN
############################
def main(argv):

   hostname = 'localhost'
   topic_pub = 'test'

   # --- Begin start mqtt client
   id = "id_%s" % (datetime.utcnow().strftime('%H_%M_%S'))
   subscriber = mqttClient("localhost", 1883, id)

   #Read other measure interval from parameter data
   paramArray = json.loads(argv[0])
   for param in paramArray:
      if not ('name' in param and 'value' in param):
          continue
      elif param["name"] == "sensor":
         subscriber.setSensor(param["value"]) 
      elif param["name"] == "id":
         subscriber.setTopic(param["value"])
      elif param["name"] == "axis":
         subscriber.setAxis(param["value"]) 

   configFileName = "connections.txt"
   topics = []
   brokerIps = []
   configExists = False

   configFile = os.path.join(os.getcwd(), configFileName)

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
   topic_pub = topics [0]
   topic_splitted = topic_pub.split('/')
   component = topic_splitted [0]
   component_id = topic_splitted [1]

   subscriber.startSubscriber()

   time.sleep(5.0)

   publisher = mqttClient(hostname, 1883, id)
   publisher.startPublisher()

   try:  
      while True:
         # messages in json format

         t = datetime.utcnow().strftime('%Y-%m-%d %H:%M:%S.%f')[:-3]

         value = subscriber.getLastValue()

         msg_pub = {"component": component.upper(), "id": component_id, "value": {"value": float(value)}}
         publisher.sendMessage (topic_pub, json.dumps(msg_pub))

         time.sleep(5)

   except:
      e = sys.exc_info()
      print ("end due to: ", str(e))
      
if __name__ == "__main__":
   main(sys.argv[1:])
