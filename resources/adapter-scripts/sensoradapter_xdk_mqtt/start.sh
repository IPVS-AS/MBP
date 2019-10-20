#!/bin/bash
DIR=`dirname $0`
cd $DIR
nohup python3 sensoradapter_xdk_mqtt.py $2 > start.log &
echo $! > pid.txt
