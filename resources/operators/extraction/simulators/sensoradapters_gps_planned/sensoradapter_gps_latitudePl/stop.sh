#!/bin/bash
sudo kill -9 $(ps -ef | grep sensoradapter_gpspl_latitude.jar | grep -v grep | awk '{print $2}')