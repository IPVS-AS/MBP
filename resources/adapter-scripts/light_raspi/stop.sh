#!/bin/bash
sudo kill -9 $(ps -ef | grep sensoradapter_light_hw.py | grep -v grep | awk '{print $2}')