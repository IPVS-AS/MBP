#!/bin/bash
cd $1
nohup python3 sensoradapter_temperature_stub.py $2 > start.log &
