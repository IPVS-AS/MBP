#!/bin/bash
sudo kill -9 $(ps -ef | grep cpu-temperature_raspberry-pi.py | grep -v grep | awk '{print $2}')