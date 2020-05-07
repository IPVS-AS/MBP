#!/bin/bash
DIR=`dirname $0`
cd $DIR
nohup python3 bosch-xdk_mqtt-gateway.py $2 > start.log &
echo $! > pid.txt
