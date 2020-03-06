import paho.mqtt.client as mqtt
from datetime import datetime
import json

############################
# MQTT Client
############################
class Client(object):
   # --- Placeholders
   hostname = 'localhost'
   port = 1883
   clientid = ''

   # --- Last message received from subscribed channel
   #     Useful just when using a Subscriber
   lastMessage = None

   def __init__(self, hostname, port, clientid):
      self.hostname = hostname
      self.port = port
      self.clientid = clientid

      self.lastMessage = None

      # create MQTT client and set user name and password 
      self.client = mqtt.Client(client_id=self.clientid, clean_session=True, userdata=None, protocol=mqtt.MQTTv31)
      #client.username_pw_set(username="use-token-auth", password=mq_authtoken)

      # set mqtt client callbacks
      self.client.on_connect = self.on_connect
      self.client.on_message = self.on_message

   # The callback for when the client receives a CONNACK response from the server.
   def on_connect(self, client, userdata, flags, rc):
      print("[" + datetime.utcnow().strftime('%Y-%m-%d %H:%M:%S.%f')[:-3] + "]: " + "ClientID: " + self.clientid + "; Connected with result code " + str(rc))

   # --- Callback when a message has been received on the subscribed topic
   def on_message(self, client, userdata, message):
      # --- Parse message to JSON
      parsed_json = json.loads(message.payload.decode("utf-8", "ignore"))

      # --- Set last messave received
      self.setLastMessage(parsed_json)

   def setLastMessage(self, message):
      self.lastMessage = message

   def getLastMessage(self):
      return self.lastMessage

   # publishes message to MQTT broker
   def sendMessage(self, topic, msg):
      self.client.publish(topic=topic, payload=msg, qos=0, retain=False)
      print(msg)

   def subscribe(self, topic):
      self.client.subscribe(topic)

   def connect(self):
      self.client.connect(self.hostname, self.port, 60)

   # connects to MQTT Broker
   def start(self):
      #runs a thread in the background to call loop() automatically.
      #This frees up the main thread for other work that may be blocking.
      #This call also handles reconnecting to the broker.
      #Call loop_stop() to stop the background thread.
      self.client.loop_start()

   