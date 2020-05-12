#!/bin/bash
DIR=`dirname $0`
cd $DIR
nohup python3 camera_raspberry-pi.py $2 > start.log &
echo $! > pid.txt
