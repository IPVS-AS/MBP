#!/usr/bin/env python3
 
import argparse
import re
import logging
import sys
import time

import mi_flora as mi_flora
import utils as utils
 
class PLANT(object):

	mac = None
	firmware = None
	name = None

	temperature = None
	conductivity = None
	light = None
	battery = None
	moisture = None

	last_update = None

	MinMax = {
		'moisture': { 'min': 0, 'max': 0},
		'light': { 'min': 0, 'max': 0},
		'conductivity': { 'min': 0, 'max': 0},
		'temperature': { 'min': 0, 'max': 0},
	}

	def __init__(self, mac):
		self.setMac(mac)

	def setMac(self, mac):
		self.mac = mac

	def updateValues(self):
		values = self.poll()

		self.temperature = values['temperature']
		self.moisture = values['moisture']
		self.light = values['light']
		self.battery = values['battery']
		self.conductivity = values['conductivity']

		self.last_update =  utils.getNowTime()

	def poll(self):
		return mi_flora.poll(self.mac)

	def setMinMax(self, min, max, attribute):
		self.MinMax[attribute]['min'] = min
		self.MinMax[attribute]['max'] = max

	def getRelativeValue(self, attribute, value):
		return (100.0 * value) / self.MinMax[attribute]['max']


	def getAttb(self, attb):
		if attb == "moisture":
			return self.moisture
		elif attb == "light":
			return self.light
		elif attb == "temperature":
			return self.temperature
		elif attb == "conductivity":
			return self.conductivity



