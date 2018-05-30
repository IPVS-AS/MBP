#!/bin/bash
cd $1
nohup python sensoradapter_temperature_stub.py > start.log &