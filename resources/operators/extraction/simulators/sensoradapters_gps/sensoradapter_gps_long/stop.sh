#!/bin/bash
sudo kill -9 $(ps -ef | grep sensoradapter_gps_long.jar | grep -v grep | awk '{print $2}')