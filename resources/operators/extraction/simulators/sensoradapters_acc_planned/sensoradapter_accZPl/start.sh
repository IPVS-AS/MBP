#!/bin/bash
cd $1
sudo nohup java -jar AccZPlSim.jar $2 > start.log &