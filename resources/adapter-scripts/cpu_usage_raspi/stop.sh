#!/bin/bash
sudo kill -9 $(ps -ef | grep sensoradapter_cpu_usage.py | grep -v grep | awk '{print $2}')