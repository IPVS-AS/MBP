#!/bin/bash
echo "STARTED ENTRYPOINT SCRIPT OF MBP"
./start_mbp.sh &
./create_device.sh &
wait
echo "EXITING ENTRYPOINT SCRIPT OF MBP"
