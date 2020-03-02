#!/bin/bash
cd $1
nohup python3 temperature_stub_param.py $2 > start.log &