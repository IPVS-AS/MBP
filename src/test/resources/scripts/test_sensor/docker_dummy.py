#!/usr/bin/env python
# -*- coding: utf-8 -*-
from mbp_client import MBPclient
import sys
import time
import random

INTERVAL_BETWEEN_SENDING_DATA = 1

def main(argv):
    mbp = MBPclient()
    mbp.connect()
    try:
        count = 0
        while True:
            count = count + 1
            mbp.send_data("{\"value\":" + str(count) + "}")
            time.sleep(INTERVAL_BETWEEN_SENDING_DATA)
    except:
        error = sys.exc_info()
        print ('Error:', str(error))

    mbp.finalize()

if __name__ == "__main__":
   main(sys.argv[1:])
