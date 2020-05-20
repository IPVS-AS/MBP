#!/bin/bash
cd $1
sudo nohup java -jar PlannedTempSim.jar $2 > start.log &