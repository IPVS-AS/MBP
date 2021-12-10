#!/bin/bash
runningPID=$(ps -ef | grep test_all_mbp_data_types.py | grep -v grep | awk '{print $2}');
if [[ $runningPID != "" ]]; then
   echo "true"; #is running
else
   echo "false"; # is not running
fi
