#!/bin/bash
DIR=`dirname $0`
cd $DIR
nohup python3 sensoradapter_flowercare.py $2 > start.log &
echo $! > pid.txt
