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

import re
from bluepy import btle
from bluepy.btle import Peripheral, Scanner, DefaultDelegate, BTLEException
from binascii import hexlify
import binascii

from threading import Thread

############################
# MQTT Client
############################
class mqttClient(object):
   hostname = 'localhost'
   port = 1883
   clientid = ''

   def __init__(self, hostname, port, clientid):
      self.hostname = hostname
      self.port = port
      self.clientid = clientid

      # create MQTT client and set user name and password 
      self.client = mqtt.Client(client_id=self.clientid, clean_session=True, userdata=None, protocol=mqtt.MQTTv31)
      #client.username_pw_set(username="use-token-auth", password=mq_authtoken)

      # set mqtt client callbacks
      self.client.on_connect = self.on_connect

   # The callback for when the client receives a CONNACK response from the server.
   def on_connect(self, client, userdata, flags, rc):
      print("[" + datetime.utcnow().strftime('%Y-%m-%d %H:%M:%S.%f')[:-3] + "]: " + "ClientID: " + self.clientid + "; Connected with result code " + str(rc))

   # publishes message to MQTT broker
   def sendMessage(self, topic, msg):
      self.client.publish(topic=topic, payload=msg, qos=0, retain=False)
      print(msg)

   # connects to MQTT Broker
   def start(self):
      self.client.connect(self.hostname, self.port, 60)

      #runs a thread in the background to call loop() automatically.
      #This frees up the main thread for other work that may be blocking.
      #This call also handles reconnecting to the broker.
      #Call loop_stop() to stop the background thread.
      self.client.loop_start()

class Bluepy(DefaultDelegate):
   def __init__(self):
      DefaultDelegate.__init__(self)

      self._peripheral_address = None
      self._peripheral_address_type = btle.ADDR_TYPE_PUBLIC
      self._peripheral = None

      self._scanner = Scanner().withDelegate(self)
      self._devicesScanned = []

      self._service_uuid = "b9e875c0-1cfa-11e6-b797-0002a5d5c51b"
      self._char_read_uuid = "1ed9e2c0-266f-11e6-850b-0002a5d5c51b"
      self._char_write_uuid = "0c68d100-266f-11e6-b388-0002a5d5c51b"

      self._descs = None
      self._svc = None

      self._ch_read = None
      self._ch_write = None

      self.lastValue = {}
      self.lastValue['value'] = 0
      self.lastValue['datetime'] = "placeholder"

   def reset(self):
      self._peripheral = None
      self._descs = None
      self._svc = None

      self._ch_read = None
      self._ch_write = None

   def scan(self, time=3):

      devices = self._scanner.scan(time)

      for dev in devices:
         print ("Device "+dev.addr+" ("+dev.addrType+"), RSSI="+str(dev.rssi)+" dB")
         for (adtype, desc, value) in dev.getScanData():
            if desc == "Complete Local Name":
               self._devicesScanned.append({
                  'name': value,
                  'id': dev.addr
               })

   def findXDKAddress(self):
      for dev in self._devicesScanned:
         if dev['name'].startswith( 'XDK' ):
            self._peripheral_address = dev['id']

      return self._peripheral_address

   def setXDKAddress(self, xdk_mac):
      self._peripheral_address = xdk_mac

   def handleNotification(self, cHandle, data):

      data = re.findall(r'\d+', str(data))[0]

      self.lastValue['value'] = data
      self.lastValue['datetime'] = datetime.utcnow().strftime('%Y-%m-%d %H:%M:%S.%f')[:-3]

      print ("Received data: "+ str(data))

      #self.lastValue = int(data)

   def handleDiscovery(self, dev, isNewDev, isNewData):
      if isNewDev:
         print ("Discovered device "+ dev.addr)
      elif isNewData:
         print ("Received new data from"+ dev.addr)

   def connect(self):

      triesCounter = 0

      while self._peripheral == None :

         try:
            print ('\nConnecting...')
            self._peripheral = Peripheral(self._peripheral_address, "random")
            if(self._peripheral != None):
               print ('Connected!')

         except BTLEException as e:
            triesCounter = triesCounter + 1
            print ("Number of tries: "+str(triesCounter))
            print ("Trying to connect again after 5s\n")
            print(e)
            time.sleep(5)

   def setDelegate(self):
      self._peripheral.setDelegate(self)

   def discoverSvc(self):
      while self._svc == None or self._descs == None or self._ch_read == None or self._ch_write == None:
         print ("\nFinding service...")
         self._svc = self._peripheral.getServiceByUUID(self._service_uuid)
         print ("Finding descriptors...")
         self._descs = self._svc.getDescriptors()
         print ("Finding read characteristic...")
         self._ch_read = self._svc.getCharacteristics(self._char_read_uuid)[0]
         print ("Finding write characteristic...\n")
         self._ch_write = self._svc.getCharacteristics(self._char_write_uuid)[0]

      print ("All characteristics found!!!\n")


   def enableSensor(self):
      print ("Turning sensor on...\n")
      self._ch_write.write(b'\x31')
      time.sleep(1.0)

   def disableSensor(self):
      print ("Turning sensor off...\n")
      self._ch_write.write(b'\x30')
      time.sleep(1.0)

   def disconnect(self):
      print ('Disconnecting...')

      self._peripheral.disconnect()

      self._peripheral = None
      print ('Disconnected!\n')
   
   def readValues(self):
      print ("Reading values...\n")

      count = 0
      while True:
         if self._peripheral.waitForNotifications(5.0):
            count = count + 1
            continue

   def BluetoothFlow(self):

      self.scan(3)
      #time.sleep(3.0)

      xdk_address = self.findXDKAddress()
      print ("\nXDK MAC: "+ xdk_address)

      disconnectCounter = 0
      active = True
      
      while active == True:
         try:
            self.reset()
            
            self.connect()
               
            self.setDelegate()

            self.discoverSvc()

            self.enableSensor()
                  
            try:
               self.readValues()
            except BTLEException as e:
               print(e)
               disconnectCounter = disconnectCounter + 1
               print ("\nDisconnection number: "+str(disconnectCounter))
               print ("\nTrying to reconnect after 5s...")
               time.sleep(5.0)

            except KeyboardInterrupt:
               active = False
               print ("\nStopping...")

         except BTLEException as e:
            print(e)

      self.disableSensor()
      self.disconnect()

   def getLastValue(self):
      return (self.lastValue)
      
############################
# MAIN
############################
def main(argv):

   xdk = Bluepy()
   
   paramArray = json.loads(argv[0])
   for param in paramArray:
      if not ('name' in param and 'value' in param):
         continue
      elif param["name"] == "mac":
         setXDKAddress(param["value"])

   bluepy_thread = Thread(target=xdk.BluetoothFlow)
   bluepy_thread.name = "BluetoothFlow"
   bluepy_thread.daemon = True

   bluepy_thread.start()

   #Time to stabilize
   time.sleep(20)

   lastValueSent = {}
   lastValueSent['value'] = 0
   lastValueSent['datetime'] = "placeholder"

   configFileName = "connections.txt"
   topics = []
   brokerIps = []
   configExists = False

   hostname = 'localhost'
   topic_pub = 'test'
   
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
   publisher = mqttClient(hostname, 1883, id)
   publisher.start()

   try:  
      while True:
         # messages in json format
         # send message, topic: temperature
         t = datetime.utcnow().strftime('%Y-%m-%d %H:%M:%S.%f')[:-3]
         lastValue = xdk.getLastValue()

         if(lastValue and lastValueSent):

            if(lastValue['datetime'] != lastValueSent['datetime']):
               
               msg_pub = {"component": component.upper(), "id": component_id, "value": "%f" % (float(lastValue['value'])) }
               publisher.sendMessage (topic_pub, json.dumps(msg_pub))

               lastValueSent['value'] = lastValue['value']
               lastValueSent['datetime'] = lastValue['datetime']
               print("Value main: " + str(lastValue['value']))

         time.sleep(30)

   except:
      e = sys.exc_info()
      print ("end due to: ", str(e))
      
if __name__ == "__main__":
   main(sys.argv[1:])
