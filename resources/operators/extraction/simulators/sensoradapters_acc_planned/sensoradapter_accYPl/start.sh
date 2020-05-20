#!/bin/bash
cd $1
sudo nohup java -jar AccYPlSim.jar $2 > start.log &