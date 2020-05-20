#!/bin/bash
cd $1
sudo nohup java -jar AccYSim.jar $2 > start.log &