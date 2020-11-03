#!/bin/bash
cd $1
sudo nohup python3 rerun.py $2> start.log &

