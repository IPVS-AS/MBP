#! /usr/bin/env python
''' taken from http://stackoverflow.com/questions/27210396/python-scapy-arp-scanning-subnet-script-from-a-book '''

from scapy.all   import srp
from scapy.all import Ether, ARP, conf

import sys

def arpingstub(iprange="10.0.1.0/24"):
    collection = [["100.70.2.139", "B8:86:87:D1:07:29"], ["100.70.2.138", "b8:27:eb:c4:ff:96"], ["100.70.2.140", "b8:27:eb:91:aa:c3"]]
    return collection

if __name__ == "__main__":
    if len(sys.argv) > 1:
        for ip in sys.argv[1:]:
            print ("arping", ip)
            print (arping(ip))

    else:
        print (arping())