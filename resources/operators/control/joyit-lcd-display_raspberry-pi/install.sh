#!/bin/bash
paho_exist=$(pip3 list | grep -F paho-mqtt)
sudo apt-get update;
if [ -z $paho_exist ]; then
 sudo apt-get install -y python3;
 sudo apt-get install -y python3-pip;
 pip3 install paho-mqtt;
fi
sudo apt-get install -y espeak; # install text-to-speech (TTS) engine
sudo pip3 install RPLCD; # install library to control LCD display

echo "$1 = $2" > $3/connections.txt;
