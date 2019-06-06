#!/bin/bash
cd $1
nohup python3 sensoradapter_cpu_usage.py > start.log &