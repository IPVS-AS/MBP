#!/bin/bash
sudo kill -9 $(ps -ef | grep sensoradapter_gpspl_longitude.jar | grep -v grep | awk '{print $2}')