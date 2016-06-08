#! /usr/bin/env python
import sys
import logging
import csv

''' taken from http://stackoverflow.com/questions/27210396/python-scapy-arp-scanning-subnet-script-from-a-book '''
from scapy.all   import srp
from scapy.all import Ether, ARP, conf

# logging setter
logger = logging.getLogger(__name__)

def loginit(logger, name):    
    hdlr = logging.FileHandler(name + '.log')
    formatter = logging.Formatter('%(asctime)s %(levelname)s %(message)s')
    hdlr.setFormatter(formatter)
    logger.addHandler(hdlr)
    logger.setLevel(logging.DEBUG)

def arping(iprange="10.0.1.0/24"):
    """Arping function takes IP Address or Network, returns nested mac/ip list"""

    #conf, verb = 0
    ans, unans = srp(Ether(dst="ff:ff:ff:ff:ff:ff")/ARP(pdst=iprange), timeout=2)

    collection = []
    for snd, rcv in ans:
        result = rcv.sprintf(r"%ARP.psrc% %Ether.src%").split()
        collection.append(result)
    return collection

def generateCSV(filename, listOfTuples):
    logger.info('generateCSV args:' + str(filename) + ',' + str(listOfTuples))

    logger.info('opening file:' + str(filename) + '.csv')
    target = open(filename + '.csv', 'w')
    logger.info('file opened:' + str(filename) + '.csv')

    logger.info('started')

    try:
        csv_out = csv.writer(target, delimiter=',', lineterminator='\n')
        csv_out.writerow(['#IP_ADDR','#MAC_ADDR'])
        for row in listOfTuples:
            csv_out.writerow(row)

    except Exception as error:
        logger.exception(__name__)

if __name__ == "__main__":    
    loginit(logger, __name__)
    logger.info('logger setted')

    try:
        if len(sys.argv) > 1:
            for ip in sys.argv[1:]:
                logger.info('arping:' + str(ip))
                result = arping(ip)
                logger.info('arping result:' + str(result))

        else:
            logger.info('arping:noargs')
            result = arping()
            logger.info('arping result:' + str(result))

        logger.info('generateCSV:' + str(result))
        generateCSV('result', result)
        logger.info('generateCSV:returned')
    except Exception as error:
        logger.exception(__name__)