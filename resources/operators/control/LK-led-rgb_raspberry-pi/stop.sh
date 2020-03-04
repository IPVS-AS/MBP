#!/bin/bash
sudo kill -9 $(ps -ef | grep LK-led-rgb_raspberry-pi.py | grep -v grep | awk '{print $2}')
