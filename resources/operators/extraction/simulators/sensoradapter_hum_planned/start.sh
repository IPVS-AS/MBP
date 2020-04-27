#!/bin/bash
cd $1
sudo nohup java -jar HumSimPlanned.jar $2 > start.log &