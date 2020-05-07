#!/bin/bash
cd $1
nohup python3 cpu-usage_raspberry-pi.py > start.log &