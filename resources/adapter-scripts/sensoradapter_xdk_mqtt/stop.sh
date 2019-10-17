#!/bin/bash
DIR=`dirname $0`
PID=`cat $DIR/pid.txt`
sudo kill -9 $PID
sudo rm $DIR/pid.txt
