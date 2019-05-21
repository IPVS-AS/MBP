#!/bin/bash
runningPID=$(ps -ef | grep sensoradapter_cpu_temperature.py | grep -v grep | awk '{print $2}');
if [[ $runningPID != "" ]]; then
   echo "true"; #is running
else
   echo "false"; # is not running
fi
