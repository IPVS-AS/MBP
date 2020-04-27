#!/bin/bash
cd $1
sudo nohup java -jar HumSim.jar $2 > start.log &