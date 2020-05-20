#!/bin/bash
cd $1
sudo nohup java -jar sensoradapter_gpspl_hight.jar $2 > start.log &