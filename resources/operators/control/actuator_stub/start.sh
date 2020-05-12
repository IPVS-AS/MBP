#!/bin/bash
cd $1
nohup python3 actuator_stub.py > start.log &