#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import sys
import paho.mqtt.client as mqtt
from datetime import datetime
import time
from rpi_ws281x import *
import os
import argparse

########## Config ##########
PORT = 1883
CONNECTIONS_FILE = "connections.txt"
TOPIC_ACTION = "action/%s/#"
ACTION_LOG_FILE = "actions.txt"

LED_COUNT      = 3      # Number of LED pixels.
LED_PIN        = 12      # GPIO pin connected to the pixels (18 uses PWM!).
#LED_PIN        = 10      # GPIO pin connected to the pixels (10 uses SPI /dev/spidev0.0).
LED_FREQ_HZ    = 800000  # LED signal frequency in hertz (usually 800khz)
LED_DMA        = 10      # DMA channel to use for generating signal (try 10)
LED_BRIGHTNESS = 255     # Set to 0 for darkest and 255 for brightest
LED_INVERT     = False   # True to invert the signal (when using NPN transistor level shift)
LED_CHANNEL    = 0       # set to '1' for GPIOs 13, 19, 41, 45 or 53
############################

class MQTTClient(object):

    def __init__(self, hostname, port, client_id):
        self.hostname = hostname
        self.port = port

        self.strip = None

        # Create MQTT client
        self.client = mqtt.Client(client_id=client_id, clean_session=True, userdata=None, protocol=mqtt.MQTTv31)

        # Register callback functions
        self.client.on_connect = self._on_connect
        self.client.on_message = self._on_message

        self.startLED()

    def _on_connect(self, client, userdata, flags, rc):
        print("Connected with result code " + str(rc))

    def _on_message(self, client, userdata, message):
        # Convert message payload to string
        message_string = message.payload.decode(encoding='UTF-8')

        print(message_string)
        self.rainbowCycle(self.strip)
        self.colorWipe(self.strip, Color(0,0,0), 10)

        # Open actions log file and append message
        with open(ACTION_LOG_FILE, "a") as file:
            file.write(message_string)
            file.write("\n\n")

    def subscribe(self, topic):
        self.client.subscribe(topic)

    def start(self):
        # Start MQTT client
        self.client.connect(self.hostname, self.port, 60)
        self.client.loop_start()

    def startLED(self):
        # Create NeoPixel object with appropriate configuration.
        self.strip = Adafruit_NeoPixel(LED_COUNT, LED_PIN, LED_FREQ_HZ, LED_DMA, LED_INVERT, LED_BRIGHTNESS, LED_CHANNEL)
        # Intialize the library (must be called once before other functions).
        self.strip.begin()

    def rainbowCycle(self, strip, wait_ms=20, iterations=1):
        """Draw rainbow that uniformly distributes itself across all pixels."""
        for j in range(256*iterations):
            for i in range(strip.numPixels()):
                strip.setPixelColor(i, self.wheel((int(i * 256 / strip.numPixels()) + j) & 255))
            strip.show()
            time.sleep(wait_ms/1000.0)

    def wheel(self, pos):
        """Generate rainbow colors across 0-255 positions."""
        if pos < 85:
            return Color(pos * 3, 255 - pos * 3, 0)
        elif pos < 170:
            pos -= 85
            return Color(255 - pos * 3, 0, pos * 3)
        else:
            pos -= 170
            return Color(0, pos * 3, 255 - pos * 3)

    def colorWipe(self, strip, color, wait_ms=50):
        """Wipe color across display a pixel at a time."""
        for i in range(strip.numPixels()):
            strip.setPixelColor(i, color)
            strip.show()
            time.sleep(wait_ms/1000.0)


def main(argv):

    # Get path to connections file
    connections_file_path = os.path.join(os.getcwd(), CONNECTIONS_FILE)

    # Wait for connections file
    while not os.path.exists(connections_file_path):
        time.sleep(1)

    # Holds extracted topics/broker ips
    topics = []
    broker_ips = []

    # Parse connections file
    with open(connections_file_path, "r") as file:
        # Read connections file line by line
        for line in file.readlines():
            # Extract topic and ip address
            splits = line.split('=')
            ex_topic = splits[0].strip('\n').strip()
            ex_ip = splits[1].strip('\n').strip()

            # Update lists
            topics.append(ex_topic)
            broker_ips.append(ex_ip)

    # Choose hostname/publish topic for MQTT client
    hostname = broker_ips[0]
    topic_pub = topics[0]

    # Get id of the component that hosts this adapter
    component_id = topic_pub.split('/')[1]

    # Generate client id
    client_id = "id_%s" % (datetime.utcnow().strftime('%H_%M_%S'))

    # Generate action topic to subscribe to
    topic_sub = TOPIC_ACTION % component_id

    # Create and start MQTT client
    mqtt_client = MQTTClient(hostname, PORT, client_id)
    mqtt_client.start()
    mqtt_client.subscribe(topic_sub)

    # Keep script running
    while True:
        time.sleep(1)


# Call main function
if __name__ == "__main__":
    main(sys.argv[1:])

