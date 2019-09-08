#!/bin/bash
cd $1
nohup python3 sensoradapter_xdk_mqtt.py $2 > start.log &
