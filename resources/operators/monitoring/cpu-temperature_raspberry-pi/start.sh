#!/bin/bash
cd $1
nohup python3 cpu-temperature_raspberry-pi.py > start.log &