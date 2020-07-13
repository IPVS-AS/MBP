#!/bin/bash
cd $1
nohup python3 Adafruit_DHT-temperature_raspberry-pi.py > start.log &