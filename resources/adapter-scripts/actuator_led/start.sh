#!/bin/bash
cd $1
nohup python2 actuator_led.py > start.log &
