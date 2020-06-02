#!/bin/bash
cd $1
sudo nohup java -jar AccXPlSim.jar $2 > start.log &