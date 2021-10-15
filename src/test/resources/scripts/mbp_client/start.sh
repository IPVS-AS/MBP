#!/bin/bash

DIR=`dirname $0`
cd $DIR

#GATEWAY=$(ip route | grap default | awk '{split($0,a," "); print(a[4])}')
#sed -i "2s/.*/brokerHost=$GATEWAY/" mbp.properties

# ---------Attention----------------
# If you rename the main python file of the operator, update the content of the entry-file-name accordingly
ENTRY_FILE_NAME=`cat $DIR/entry-file-name`
#-----------------------------------

tmux new-session -d -s "scriptSession" "python3 $ENTRY_FILE_NAME $2 | tee -a /home/mbp/app.log"
