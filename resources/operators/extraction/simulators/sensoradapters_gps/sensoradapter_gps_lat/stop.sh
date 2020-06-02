#!/bin/bash
sudo kill -9 $(ps -ef | grep sensoradapter_gps_lat.jar | grep -v grep | awk '{print $2}')