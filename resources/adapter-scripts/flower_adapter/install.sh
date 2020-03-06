#!/bin/bash
paho_exist=$(pip3 list | grep -F paho-mqtt)
echo $paho_exist >> install.log;

if [ -z $paho_exist ]; then
 sudo apt-get update;
 sudo apt-get install -y python3;
 sudo apt-get install -y python3-pip;
 pip3 install paho-mqtt;
fi
sudo pip3 install miflora

echo "$1 = $2" > $3/connections.txt;
