#!/bin/bash
cd $1
nohup python3 sensoradapter_esp.py $2 > start.log &
