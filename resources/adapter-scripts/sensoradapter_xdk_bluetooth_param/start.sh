#!/bin/bash
cd $1
nohup python3 sensoradapter_xdk_bluetooth_param.py $2 > start.log &
