#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import sys
import paho.mqtt.client as mqtt
from datetime import datetime
import time
import os
from RPLCD import CharLCD
import RPi.GPIO as GPIO

########## Config ##########
PORT = 1883
CONNECTIONS_FILE = "connections.txt"
TOPIC_ACTION = "action/%s/#"
ACTION_LOG_FILE = "actions.txt"
TEXT_1 = "Press button two for weather forecast"
TEXT_2 = "Button 2 was pressed"
TEXT_3 = "Button 3 was pressed"
TEXT_4 = "Button 4 was pressed"
############################

class MQTTClient(object):

    def __init__(self, hostname, port, client_id):
        self.hostname = hostname
        self.port = port

        # Create MQTT client
        self.client = mqtt.Client(client_id=client_id, clean_session=True, userdata=None, protocol=mqtt.MQTTv31)

        # Register callback functions
        self.client.on_connect = self._on_connect
        self.client.on_message = self._on_message

    def _on_connect(self, client, userdata, flags, rc):
        print("Connected with result code " + str(rc))

    def _on_message(self, client, userdata, message):
        # Convert message payload to string
        message_string = message.payload.decode(encoding='UTF-8')

        # Open actions log file and append message
        with open(ACTION_LOG_FILE, "a") as file:
            file.write(message_string)
            file.write("\n\n")

        #action sent to speaker:
        text1 = "A new message has been received."
        os.system("espeak -ven+f5 -s130 \'" + text1 + "\' --stdout | aplay")
        msg_json = json.loads(message_string)
        msg_data = msg_json['data']
        os.system("espeak -ven+f5 -s130 \'" + msg_data + "\' --stdout | aplay")
  
    def subscribe(self, topic):
        self.client.subscribe(topic)

    def start(self):
        # Start MQTT client
        self.client.connect(self.hostname, self.port, 60)
        self.client.loop_start()


def initButtons():
        GPIO.setwarnings(False)
        GPIO.setmode(GPIO.BCM)
        GPIO.setup(4, GPIO.IN, pull_up_down=GPIO.PUD_UP)
        GPIO.setup(23, GPIO.IN, pull_up_down=GPIO.PUD_UP)
        GPIO.setup(10, GPIO.IN, pull_up_down=GPIO.PUD_UP)
        GPIO.setup(9, GPIO.IN, pull_up_down=GPIO.PUD_UP)

def checkSwitch():
        v0 = not GPIO.input(4)
        v1 = not GPIO.input(23)
        v2 = not GPIO.input(10)
        v3 = not GPIO.input(9)
        return v3, v0, v1, v2

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

    # setup LCD
    lcd = CharLCD(pin_rs=7, pin_e=8, pins_data=[17, 18, 27, 22], numbering_mode=GPIO.BCM, cols=16, rows=2, dotsize=8)
    lcd.write_string('* TPL IoT Lab * > Press a button')
    initButtons()

    # Keep script running
    while True:
        v = checkSwitch()
        #print ("Button 1: ", v[0], " Button 2: ", v[1], " Button 3: ", v[2], " Button 4: ", v[3])

        if (v[0] == True):
            os.system("espeak -ven+f5 -s130 \'" + TEXT_1 + "\' --stdout | aplay")
        elif (v[1] == True):
            TEXT_2 = os.popen("curl http://wttr.in/Stuttgart?format=\'Current+temperature+in+%l:+%t+%C\'").readline()
            os.system("espeak -ven+f5 -s130 \'" + TEXT_2 + "\' --stdout | aplay")
        elif (v[2] == True):
            response = os.popen('vcgencmd measure_temp').readline()
            TEXT_3 = (res.replace("temp=","The CPU temperature of this Raspberry Pi is ").replace("'C\n"," Celsius degrees"))
                os.system("espeak -ven+f5 -s130 \'" + TEXT_3 + "\' --stdout | aplay")
        elif (v[3] == True):
                os.system("espeak -ven+f5 -s130 \'" + TEXT_4 + "\' --stdout | aplay")

        time.sleep(1)


# Call main function
if __name__ == "__main__":
    main(sys.argv[1:])
