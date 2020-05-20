#!/bin/bash
cd $1
sudo nohup java -jar sensoradapter_gps_long.jar $2 > start.log &