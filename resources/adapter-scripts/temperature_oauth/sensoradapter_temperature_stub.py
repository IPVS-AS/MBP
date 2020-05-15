#!/usr/bin/env python
# -*- coding: utf-8 -*-
import sys, getopt
import paho.mqtt.client as mqtt
from datetime import datetime
import time
import json
import os, fnmatch
from os.path import expanduser
import random
import oauth2_token_manager
from threading import Timer

############################
# MQTT Client
############################
class mqttClient(object):
   hostname = ''
   port = 1883
   
   # Enter client id here:
   client_id = 'device-client'

   # Enter client secret here:
   client_secret = 'device'

   authorization_code = ''
   refresh_token = ''
   token_url = ''
   token_url_port = 8080


   def __init__(self, hostname, port, clientid, token, refresh):
      self.hostname = hostname
      self.port = port
      self.clientid = clientid
      self.token = token
      self.token_url = 'http://' + str(hostname) + ':' + str(self.token_url_port) + '/MBP/oauth/token'

      # create MQTT client and set user name and password 
      self.client = mqtt.Client(client_id=self.clientid, clean_session=True, userdata=None, protocol=mqtt.MQTTv31)
      if (not refresh):
         print(self.token_url)
         access_token, refresh_token = oauth2_token_manager.get_access_token(self.token_url, self.client_id, self.client_secret, self.token)
         mqttClient.refresh_token = refresh_token
         self.username = access_token
      else:
         access_token, refresh_token = oauth2_token_manager.get_access_token_with_refresh_token(self.token_url, self.client_id, self.client_secret, self.token)
         mqttClient.refresh_token = refresh_token
         self.username = access_token
      self.client.username_pw_set(username=self.username, password="any")

      # set mqtt client callbacks
      self.client.on_connect = self.on_connect

   # The callback for when the client receives a CONNACK response from the server.
   def on_connect(self, client, userdata, flags, rc):
      print("[" + datetime.utcnow().strftime('%Y-%m-%d %H:%M:%S.%f')[:-3] + "]: " + "ClientID: " + self.clientid + "; Connected with result code " + str(rc))      

   # publishes message to MQTT broker
   def sendMessage(self, topic, msg):
      self.client.publish(topic=topic, payload=msg, qos=0, retain=False)

   # connects to MQTT Broker
   def start(self):
      self.client.connect(self.hostname, self.port, 60)

      #runs a thread in the background to call loop() automatically.
      #This frees up the main thread for other work that may be blocking.
      #This call also handles reconnecting to the broker.
      #Call loop_stop() to stop the background thread.
      self.client.loop_start()

   def stop(self):
      self.client.loop_stop()

############################
# MAIN
############################
def main(argv):

   hostname = '192.168.2.133'
   topic_pub = 'sensor/test'
   paramArray = json.loads(argv[0])
   
   # Read device_code from parameters
   for param in paramArray:
      if not ('name' in param and 'value' in param):
         continue
      elif param["name"] == "device_code":
         device_code = param["value"]
         print (device_code)

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
   
   print("Connecting to: " + hostname + " pub on topic: " + topic_pub)
   
   # --- Begin start mqtt client
   id = "id_%s" % (datetime.utcnow().strftime('%H_%M_%S'))
   publisher = mqttClient(hostname, 1883, id, device_code, False)

   refresh_token = mqttClient.refresh_token
   publisher.start()

   counter = 0

   try:  
      while True:
         if (counter == 3):
            print("Refreshing token...")
            publisher.stop()
            publisher = mqttClient(hostname, 1883, id, refresh_token, True)
            refresh_token = publisher.refresh_token
            print ("Refresh token " + refresh_token)
            publisher.start()
            counter = 0
         # messages in json format
         # send message, topic: temperature
         t = datetime.utcnow().strftime('%Y-%m-%d %H:%M:%S.%f')[:-3]
         outputValue = random.choice([20.0, 20.5, 21.0, 22.0, 22.5, 25.5, 30.0, 30.1, 31.5, 29.9, 35.0])
         msg_pub = {"component": component.upper(), "id": component_id, "value": "%f" % (outputValue) }
         publisher.sendMessage (topic_pub, json.dumps(msg_pub))

         time.sleep(30)
         counter += 1
   except:
      e = sys.exc_info()
      print ("end due to: ", str(e))
      
if __name__ == "__main__":
   main(sys.argv[1:])
