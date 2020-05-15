#!/bin/bash
cd $1
sudo nohup java -jar AccXSim.jar $2 > start.log &