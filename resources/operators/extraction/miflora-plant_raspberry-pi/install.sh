#!/bin/bash
paho_exist=$(pip3 list | grep -F paho-mqtt)
echo $paho_exist >> install.log;

if [ -z $paho_exist ]; then
 sudo apt-get update;
 sudo apt-get install -y python3;
 sudo apt-get install -y python3-pip;
 pip3 install paho-mqtt;
fi
sudo pip3 install miflora;
# installing backend bluepy for using miflora
sudo apt-get install -y libglib2.0-dev;
sudo pip3 install bluepy;
