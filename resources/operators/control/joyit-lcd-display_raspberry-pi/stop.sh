#!/bin/bash
sudo kill -9 $(ps -ef | grep joyit-lcd-display_raspberry-pi.py | grep -v grep | awk '{print $2}')