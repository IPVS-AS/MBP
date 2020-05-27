#!/bin/bash
DIR=`dirname $0`

# checks if the stored PID at starting execution of the operator is still running
if [ -f $DIR/pid.txt ]; then
   PID=`cat $DIR/pid.txt`
   if [ -n "$(ps -p  $PID -o pid=)" ]; then
      echo "true"; #is running
   else
      sudo rm $DIR/pid.txt;
      echo "false";
   fi 
else
   echo "false"; # is not running
fi
