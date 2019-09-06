#!/bin/bash
cd $1
nohup python3 sensoradapter_xdk.py $2 > start.log &
