#!/bin/bash
sudo apt-get install -y python-pip;
pip install paho-mqtt;
echo "$1 = $2" > $3/connections.txt;
