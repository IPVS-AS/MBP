#!/bin/bash
sudo kill -9 $(ps -ef | grep speaker_mqtt.py | grep -v grep | awk '{print $2}')
