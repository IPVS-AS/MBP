#!/bin/bash
runningPID=$(ps -ef | grep actuator_testingTool.py | grep -v grep | awk '{print $2}');
if [[ $runningPID != "" ]]; then
   echo "true";
else
   echo "false";
fi