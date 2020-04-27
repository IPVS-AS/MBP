#!/bin/bash
cd $1
nohup java -jar TempSim.jar $2 > start.log  &