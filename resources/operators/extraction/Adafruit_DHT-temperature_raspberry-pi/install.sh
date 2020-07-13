#!/bin/bash
sudo apt-get update;
sudo apt-get install -y python3;
sudo apt-get install -y python3-pip;
pip3 install paho-mqtt;
pip3 install spidev;
pip3 install RPi.GPIO;
pip3 install Adafruit_Python_DHT;
echo "$1 = $2" > $3/connections.txt;
