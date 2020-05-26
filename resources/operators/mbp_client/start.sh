#!/bin/bash

DIR=`dirname $0`
cd $DIR
# ---------Attention----------------
# If you rename the main python file of the operator, update the content of the entry-file-name accordingly
ENTRY_FILE_NAME=`cat $DIR/entry-file-name`
#-----------------------------------

nohup python3 $ENTRY_FILE_NAME > start.log &

# output PID of last job running in background
echo $! > pid.txt