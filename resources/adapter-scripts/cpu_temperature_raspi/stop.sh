#!/bin/bash
sudo kill -9 $(ps -ef | grep sensoradapter_cpu_temperature.py | grep -v grep | awk '{print $2}')