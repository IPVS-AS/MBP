from requests_oauthlib import OAuth2Session, TokenUpdated
from requests.auth import HTTPBasicAuth
import os
import json
import sys
import getopt
import paho.mqtt.client as mqtt
from datetime import datetime
import time
from os.path import expanduser
import random
import oauth2_token_manager
from threading import Timer

# Only for local testing if no https is available
os.environ['OAUTHLIB_INSECURE_TRANSPORT'] = '1'


def get_access_token(token_url, client_id, client_secret, authorization_code):
	auth = HTTPBasicAuth(client_id, client_secret)
	oauth = OAuth2Session(client_id)
	token = oauth.fetch_token(token_url=token_url, code=authorization_code, method='POST', auth=auth)
	return token


def get_access_token_with_refresh_token(token_url, client_id, client_secret, refresh_token):
	auth = HTTPBasicAuth(client_id, client_secret)
	client = OAuth2Session(client_id)
	token = client.refresh_token(token_url, refresh_token, auth=auth)
	return token

############################
# MQTT Client
############################


class mqttClient(object):

	token_url = ''
	token_url_port = 8080

	def __init__(self, hostname, port, clientid, code):
		self.hostname = hostname
		self.port = port
		self.client_id = 'device-client'
		self.client_secret = 'device'
		self.authorization_code = code
		self.token_url = 'http://' + \
			str(hostname) + ':' + \
				str(mqttClient.token_url_port) + '/MBP/oauth/token'

		self.token = get_access_token(
			self.token_url, self.client_id, self.client_secret, self.authorization_code)
		self.expires_in = self.token['expires_in']
		self.access_token = self.token['access_token']
		self.refresh_token = self.token['refresh_token']
		self.timestamp = time.time()

	def on_connect(self, client, userdata, flags, rc):
		print("[" + datetime.utcnow().strftime('%Y-%m-%d %H:%M:%S.%f')[:-3] + "]: " + "ClientID: " + self.client_id + "; Connected with result code " + str(rc))

	# publishes message to MQTT broker
	def sendMessage(self, topic, msg):
		if self.timestamp + self.expires_in <= time.time():
			self.stop()
			self.token = get_access_token_with_refresh_token(self.token_url, self.client_id, self.client_secret, self.refresh_token)
			self.expires_in = self.token['expires_in']
			self.access_token = self.token['access_token']
			self.refresh_token = self.token['refresh_token']
			self.timestamp = time.time()
			self.client.username_pw_set(username=self.access_token, password='any')
			self.start()
		self.client.publish(topic=topic, payload=msg, qos=0, retain=False)

	# connects to MQTT Broker
	def start(self):
		self.client.connect(self.hostname, self.port, 60)

	# runs a thread in the background to call loop() automatically.
	# This frees up the main thread for other work that may be blocking.
	# This call also handles reconnecting to the broker.
	# Call loop_stop() to stop the background thread.
		self.client.loop_start()

	def stop(self):
		self.client.loop_stop()

	def setup_oauth2(self):
		# create MQTT client and set user name and password 
		self.client = mqtt.Client(client_id=self.client_id, clean_session=True, userdata=None, protocol=mqtt.MQTTv31)
		self.client.username_pw_set(username=self.access_token, password='any')
		# set mqtt client callbacks
		self.client.on_connect = self.on_connect
		self.start()
