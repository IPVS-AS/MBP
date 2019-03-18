#!/bin/bash
sudo kill -9 $(ps -ef | grep actuatoradapter_switch_hw.py | grep -v grep | awk '{print $2}')