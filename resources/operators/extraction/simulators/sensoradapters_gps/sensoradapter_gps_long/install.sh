#!/bin/bash
paho_exist=$(pip3 list | grep -F paho-mqtt)     # grep sucht nach bestimmten Mustern in Dateien
if [ -z $paho_exist ]; then                     # Bedinung wird wahr, wenn Variable paho_exist leer ist f�hrt die folgenden Installationen durch
 sudo apt-get update;
 pip3 install paho-mqtt;
fi
echo "$1 = $2" > $3/connections.txt;            # '>' Dient der Umleitung der Standardausgabe in die connections.txt Datei