#!/usr/bin/env python
# -*- coding: utf-8 -*-
import picamera
import time
import base64
import random, string
import json
from mqttClient import mqttClient
from datetime import datetime
import math

class CAMERA(object):

	def __init__(self): 

		self.width = 160
		self.height = 128

		self.camera = picamera.PiCamera()

		self.packet_size = 3000

	def takePicture(self, name):
		try: 
			self.camera.start_preview()
			time.sleep(1)
			self.camera.capture(name, resize=(self.width, self.height))
			self.camera.stop_preview()
			pass
		finally:
			
			print("picture taken")

	def close(self):
		self.camera.close()

	def convertImageToBase64(self, name):
		try:  
			with open(name, "rb") as image_file:
				encoded = base64.b64encode(image_file.read())
				return encoded
			pass
		finally:
			print("decoded successfully")

	def randomword(self, length):
		return ''.join(random.choice(string.lowercase) for i in range(length))

	def publishEncodedImage(self, encoded, hostname, port=1883):
		id = "id_%s" % (datetime.utcnow().strftime('%H_%M_%S'))
		# --- Create instance of mqtt client
		publisher = mqttClient(hostname, port, id)
		publisher.connect()
		try:  
			# --- Divides enconded image into several packets
			end = self.packet_size
			start = 0
			length = len(encoded)
			print("random")
			picId = random.randrange(2000)
			pos = 0
			no_of_packets = math.ceil(length/self.packet_size)

			# --- Loop to send each  
			while start <= len(encoded):
				# --- Creates message to be published
				data = {"data": encoded[start:end].decode("utf-8"), "pic_id":picId, "pos": pos, "size": no_of_packets}

				# --- Publishes the message
				publisher.sendMessage("image", json.dumps(data))
				publisher.sendMessage("image", "oi")
				# --- Sleep and continues the loop
				time.sleep(0.05)
				end += self.packet_size
				start += self.packet_size
				pos = pos +1
			pass
		finally:
			print("sent successfully to broker "+str(hostname))
