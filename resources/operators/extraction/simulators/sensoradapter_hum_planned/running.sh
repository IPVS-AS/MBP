#!/bin/bash
runningPID=$(ps -ef | grep HumSimPlanned.jar | grep -v grep | awk '{print $2}');
if [[ $runningPID != "" ]]; then
   echo "true";
else
   echo "false";
fi