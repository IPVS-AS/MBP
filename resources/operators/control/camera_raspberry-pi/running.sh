#!/bin/bash
DIR=`dirname $0`

if [ -f $DIR/pid.txt ]; then
   PID=`cat $DIR/pid.txt`
   if [ -n "$(ps -p  $PID -o pid=)" ]; then
      echo "true"; #is running
   else
      echo "false";
   fi 
else
   echo "false"; # is not running
fi