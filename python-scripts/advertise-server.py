from threading import Thread
import datetime
import socket as sck
import json
import requests

import pymongo
from pymongo import MongoClient

PORT = 20123

DB_NAME = 'connde'
COLL_NAME = 'device'
AUTODEPLOY_URL = 'http://localhost:8080/connde/api/autodeploy'

NAME_SUFIX = ' (AUTO)'

def autodeploy(data):
    headers = { 'Content-Type': 'application/json'}
    r = requests.post(AUTODEPLOY_URL, headers=headers, data=json.dumps(data))
    print(str(r))

def hasAutodeploy(data):
    return data['autodeploy']

def isEqual(e1, e2):
    if e1 is None or e2 is None:
        return False

    return e1['ipAddress'] == e2['ipAddress'] and e1['iface'] == e2['iface']

def proccessMessage(coll, message):
    data = message[0].decode('utf-8')
    print('Received: ' + str(data))

    data = json.loads(data)

    if ('macAddress' in data):
        key = { 'macAddress': data['macAddress'] }

        # find old entry
        old = coll.find_one(key)
        
        # upsert new entry
        post = data
        post['name'] = str(post['macAddress'] + NAME_SUFIX)
        post['date'] = str(datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S"))
        coll.update(key, post, upsert=True)

        if (not isEqual(old, data) and hasAutodeploy(data)):
            # has modified ip / iface / config or is a new entry -> redeploy
            print('Autodeploy ' + str(data['macAddress']))
            autodeploy(data);

# main
if __name__ == "__main__":
    print ('Start')

    # bind socket for broadcasts
    s = sck.socket(sck.AF_INET, sck.SOCK_DGRAM)
    s.bind((' ', PORT))

    # access db
    client = MongoClient()
    db = client[DB_NAME]
    coll = db[COLL_NAME]
    result = coll.create_index([('macAddress', pymongo.ASCENDING)], unique=True)

    print('Waiting for messages...')
    # loop n wait for messages
    while (True):
        message = s.recvfrom(1024)

        # starts a thread and waits for next message
        t = Thread(target = proccessMessage, args=(coll, message))
        t.start()