#!/bin/bash
cd $1
nohup sudo python3 LK-led-rgb_raspberry-pi.py > start.log &
