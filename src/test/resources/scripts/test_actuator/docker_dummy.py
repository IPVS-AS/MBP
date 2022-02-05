#!/usr/bin/env python
# -*- coding: utf-8 -*-
from mbp_client import MBPclient
import sys
import time
import random

def main(argv):
    mbp = MBPclient()
    mbp.connect()
    try:
        print ('Subscribing to %s' % mbp.broker_action_topic)
        mbp.subscribe(mbp.broker_action_topic)
        print ('Waiting for actions from MBP...')
        while True:
            time.sleep(1)
    except:
        error = sys.exc_info()
        print ('Error:', str(error))

    mbp.finalize()
    print ('Exiting...')

if __name__ == "__main__":
   main(sys.argv[1:])
