#!/bin/bash
cd $1
nohup python3 sensoradapter_temperature_hw.py > start.log &