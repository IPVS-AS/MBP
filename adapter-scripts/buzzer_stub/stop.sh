#!/bin/bash
sudo kill -9 $(ps -ef | grep actuatoradapter_buzzer_stub.py | grep -v grep | awk '{print $2}')