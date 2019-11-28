#!/bin/bash
paho_exist=$(pip3 list | grep -F paho-mqtt)
if [[ -z $paho_exist ]]; then
 sudo apt-get update;
 sudo apt-get install -y python3;
 sudo apt-get install -y python3-pip;
 pip3 install paho-mqtt;
fi
echo "$1 = $2" > $3/connections.txt;