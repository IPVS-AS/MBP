#!/usr/bin/env python
# -*- coding: utf-8 -*-
from mbp_client import MBPclient
import sys
import time
import random

# default interval for sending data (seconds)
INTERVAL_BETWEEN_SENDING_DATA = 15

def main(argv):
    #default interval for sending data
    sendingInterval = [30]
    sensorVals =[]

    #Read other interval informations from parameter data
    paramArray = json.loads(argv[0])
    for param in paramArray:
        if not ('name' in param and 'value' in param):
            continue
        else:
            sendingInterval.append(param["name"])
            sensorVals.append(param["value"])

    # instantiate the MBP client
    mbp = MBPclient()
    
    # initialize the MBP client
    mbp.connect()

    try:
        # This loop ensures your code runs continuously, 
        # for example, to read sensor values regularly at a given interval.
        while True:
            for sensorValue in sensorVals:
                value = sensorValue
                print(value)

                # waits a time interval before sending new data
            for sleepTime in sendingInterval:
                time.sleep(sleepTime)

            # send data to the MBP
            mbp.send_data(value)
            


    except:
        error = sys.exc_info()
        print ('Error:', str(error))
    
    # terminate the MBP client
    mbp.finalize()

if __name__ == "__main__":
   main(sys.argv[1:])
   