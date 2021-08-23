#!/usr/bin/env python
# -*- coding: utf-8 -*-
from mbp_client import MBPclient
import sys
import time
import random

# default interval for sending data (seconds)
INTERVAL_BETWEEN_SENDING_DATA = 15

def main(argv):   
    # instantiate the MBP client
    mbp = MBPclient()
    
    # initialize the MBP client
    mbp.connect()

    try:
        # This loop ensures your code runs continuously, 
        # for example, to read sensor values regularly at a given interval.
        while True:
            #############################
            #### Your code goes here ####
            value = random.choice([20.0, 20.5, 21.0, 22.0, 22.5, 25.5, 30.0, 30.1, 31.5, 29.9, 35.0])
            #############################
            
            # send data to the MBP
            mbp.send_data("{\"value\":" + str(value) + "}")
            
            # waits a time interval before sending new data
            time.sleep(INTERVAL_BETWEEN_SENDING_DATA)
    except:
        error = sys.exc_info()
        print ('Error:', str(error))
    
    # terminate the MBP client
    mbp.finalize()

if __name__ == "__main__":
   main(sys.argv[1:])
   