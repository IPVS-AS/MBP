#!/bin/bash
cd $1
sudo nohup java -jar AccZSim.jar $2 > start.log &