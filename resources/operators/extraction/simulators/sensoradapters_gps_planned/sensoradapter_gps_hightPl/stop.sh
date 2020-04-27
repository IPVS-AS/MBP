#!/bin/bash
sudo kill -9 $(ps -ef | grep sensoradapter_gpspl_hight.jar | grep -v grep | awk '{print $2}')