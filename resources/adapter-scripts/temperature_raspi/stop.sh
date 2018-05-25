#!/bin/bash
sudo kill -9 $(ps -ef | grep sensoradapter_temperature_hw.py | grep -v grep | awk '{print $2}')