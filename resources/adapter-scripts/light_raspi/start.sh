#!/bin/bash
cd $1
nohup python3 sensoradapter_light_hw.py > start.log &