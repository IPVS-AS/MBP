#!/bin/bash
cd $1
nohup python3 sensoradapter_cpu_temperature.py > start.log &