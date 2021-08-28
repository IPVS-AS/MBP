#!/bin/bash

DIR=`dirname $0`
cd $DIR
sed -i '2s/.*/brokerHost=10.133.1.65/' mbp.properties
#sed -i '2s/.*/brokerHost=host.docker.internal/' mbp.properties

# ---------Attention----------------
# If you rename the main python file of the operator, update the content of the entry-file-name accordingly
ENTRY_FILE_NAME=`cat $DIR/entry-file-name`
#-----------------------------------

tmux new-session -d -s "scriptSession" "python3 $ENTRY_FILE_NAME $2 | tee -a /home/mbp/app.log"