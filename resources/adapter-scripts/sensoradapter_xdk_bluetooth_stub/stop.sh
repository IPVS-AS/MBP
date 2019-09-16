#!/bin/bash
sudo kill -6 $(ps -ef | grep sensoradapter_xdk_bluetooth_stub.py | grep -v grep | awk '{print $2}')
