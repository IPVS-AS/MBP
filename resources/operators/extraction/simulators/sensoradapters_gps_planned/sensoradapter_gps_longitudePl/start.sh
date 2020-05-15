#!/bin/bash
cd $1
sudo nohup java -jar sensoradapter_gpspl_longitude.jar "$2" > start.log &