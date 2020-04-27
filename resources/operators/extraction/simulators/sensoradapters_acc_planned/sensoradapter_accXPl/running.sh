#!/bin/bash
runningPID=$(ps -ef | grep AccXPlSim.jar | grep -v grep | awk '{print $2}');
if [[ $runningPID != "" ]]; then
   echo "true";
else
   echo "false";
fi