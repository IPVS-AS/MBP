#!/bin/bash
sudo kill -6 $(ps -ef | grep sensoradapter_esp.py | grep -v grep | awk '{print $2}')
