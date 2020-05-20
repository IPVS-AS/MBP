#!/bin/bash
cd $1
sudo nohup java -jar sensoradapter_gps_lat.jar $2 > start.log &