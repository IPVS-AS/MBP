#!/bin/bash
cd $1
nohup python3 actuatoradapter_switch_hw.py $2 > start.log &