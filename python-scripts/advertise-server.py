from threading import Thread
import datetime
import socket as sck
import json
import requests

import pymongo
from pymongo import MongoClient

PORT = 20123

AUTODEPLOY_URL = 'http://localhost:8080/connde/api/autodeploy'

def autodeploy(data):
    headers = { 'Content-Type': 'application/json'}
    r = requests.post(AUTODEPLOY_URL, headers=headers, data=json.dumps(data))
    print(str(r))

def hasAutodeploy(data):
    return data['autodeploy']

def isEqual(e1, e2):
    if e1 is None or e2 is None:
        return False

    return e1['ip'] == e2['ip'] and e1['iface'] == e2['iface']

def proccessMessage(coll, message):
    data = message[0].decode('utf-8')
    print('Received: ' + str(data))

    data = json.loads(data)

    if ('mac' in data):
        key = { 'mac': data['mac'] }

        # find old entry
        old = coll.find_one(key)
        
        # upsert new entry
        post = data
        post['date'] = str(datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S"))
        coll.update(key, post, upsert=True)

        if (not isEqual(old, data) and hasAutodeploy(data)):
            # has modified ip / iface / config or is a new entry -> redeploy
            print('Autodeploy ' + str(data['mac']))
            autodeploy(data);

# main
if __name__ == "__main__":

    # bind socket for broadcasts
    s = sck.socket(sck.AF_INET, sck.SOCK_DGRAM)
    s.bind((' ', PORT))

    # access db
    client = MongoClient()
    db = client.sensmonqtt
    coll = db['address']
    result = coll.create_index([('mac', pymongo.ASCENDING)], unique=True)

    print('Waiting for messages...')
    # loop n wait for messages
    while (True):
        message = s.recvfrom(1024)

        # starts a thread and waits for next message
        t = Thread(target = proccessMessage, args=(coll, message))
        t.start()