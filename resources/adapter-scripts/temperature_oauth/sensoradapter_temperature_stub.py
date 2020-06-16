#!/usr/bin/env python
# -*- coding: utf-8 -*-
import sys, getopt
from datetime import datetime
import time
import json
import os, fnmatch
from os.path import expanduser
import random
import oauth2_token_manager
from threading import Timer

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
   print(brokerIps[0])
   topic_pub = topics [0]
   topic_splitted = topic_pub.split('/')
   component = topic_splitted [0]
   component_id = topic_splitted [1]
   
   print("Connecting to: " + hostname + " pub on topic: " + topic_pub)
   
   # --- Begin start mqtt client
   id = "id_%s" % (datetime.utcnow().strftime('%H_%M_%S'))
   publisher = oauth2_token_manager.mqttClient(hostname, 1883, id, device_code)
   publisher.setup_oauth2()

   try:
      while True:
         # messages in json format
         # send message, topic: temperature
         t = datetime.utcnow().strftime('%Y-%m-%d %H:%M:%S.%f')[:-3]
         outputValue = random.choice([20.0, 20.5, 21.0, 22.0, 22.5, 25.5, 30.0, 30.1, 31.5, 29.9, 35.0])
         msg_pub = {"component": component.upper(), "id": component_id, "value": "%f" % (outputValue) }
         publisher.sendMessage(topic_pub, json.dumps(msg_pub))

         time.sleep(30)

   except:
      e = sys.exc_info()
      print ("end due to: ", str(e))
      
if __name__ == "__main__":
   main(sys.argv[1:])
