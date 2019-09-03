#!/bin/bash
sudo apt-get update;
sudo apt-get install -y python3;
sudo apt-get install -y python3-pip;
pip3 install paho-mqtt;
pip3 install oauth2;
echo "$1 = $2" > $3/connections.txt;
