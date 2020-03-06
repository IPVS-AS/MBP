#!/bin/bash
DIR=`dirname $0`
cd $DIR
nohup python3 adapter_distance.py $2 > start.log &
echo $! > pid.txt
