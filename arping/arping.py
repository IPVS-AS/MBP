#! /usr/bin/env python
import sys
import logging
import datetime
from time import sleep

from scapy.all import srp
from scapy.all import Ether, ARP, conf
import pymongo
from pymongo import MongoClient

from repeatedtimer import RepeatedTimer

client = MongoClient()
db = client.sensmonqtt
coll = db['arping']
result = coll.create_index([('mac', pymongo.ASCENDING)], unique=True)

def step():
    if len(sys.argv) > 1:
        for ip in sys.argv[1:]:
            print ("arping", ip)
            result = arping(ip)
    else:
        result = arping()
    #print (result)
    for ip, mac in result:
        save(coll=coll, ip=ip, mac=mac)

def save(coll, ip, mac):
    mac = mac.replace('-', '').replace(':', '').lower()
    key = {
        'mac': mac
    }
    post = {
        'ip':   ip,
        'mac':  mac,
        'date': datetime.datetime.utcnow()
    }
    coll.update(key, post, upsert=True)

def arping(iprange="10.0.1.0/24"):
    """Arping function takes IP Address or Network, returns nested mac/ip list"""

    #conf, verb = 0
    ans, unans = srp(Ether(dst="ff:ff:ff:ff:ff:ff")/ARP(pdst=iprange), timeout=2)

    collection = []
    for snd, rcv in ans:
        result = rcv.sprintf(r"%ARP.psrc% %Ether.src%").split()
        collection.append(result)
    return collection


if __name__ == "__main__":
    interval = 10
    try:
        rt = RepeatedTimer(interval, step)
        while True:
            sleep(interval * 100)
    except Exception as error:
        print (error)
        #log.exception(__name__)
    finally:
        rt.stop()
    