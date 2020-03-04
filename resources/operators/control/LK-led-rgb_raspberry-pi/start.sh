#!/bin/bash
cd $1
nohup python2 LK-led-rgb_raspberry-pi.py > start.log &
