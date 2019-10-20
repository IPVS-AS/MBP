#!/bin/bash
cd $1
sudo nohup python3 speaker_mqtt.py >> start.log & sleep 1;
