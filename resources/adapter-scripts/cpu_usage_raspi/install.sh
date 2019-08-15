#!/bin/bash
sudo apt-get update;
sudo apt-get install -y sysstat;
sudo apt-get install -y python3;
sudo apt-get install -y python3-pip;
pip3 install paho-mqtt;
echo "$1 = $2" > $3/connections.txt;
