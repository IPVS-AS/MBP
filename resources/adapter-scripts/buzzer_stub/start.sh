#!/bin/bash
cd $1
nohup python3 actuatoradapter_buzzer_stub.py > start.log & sleep 1