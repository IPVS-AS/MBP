from threading import Thread
import datetime
import socketserver
import socket as sck
import json
import requests
import const

import pymongo
from pymongo import MongoClient

PORT = 20123

DB_NAME = 'connde'
COLL_NAME = 'deploy_test'
AUTODEPLOY_URL = 'http://localhost:8080/connde/api/autodeploy'

NAME_SUFIX = ' (AUTO)'


def autodeploy(data):
    headers = {'Content-Type': 'application/json'}
    r = requests.post(AUTODEPLOY_URL, headers=headers, data=data)
    print(str(r))


def hasAutodeploy(data):
    return data['autodeploy']


def isEqual(e1, e2):
    if e1 is None or e2 is None:
        return False

    return e1['ipAddress'] == e2['ipAddress'] and e1['iface'] == e2['iface']


def proccessMessage(coll, message, addr_info):
    data = message.decode('utf-8')
    print('Received: ' + str(data))

    return

    data = json.loads(data)

    if ('macAddress' in data):
        key = {'macAddress': data['macAddress']}

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


class ConndeHandler(socketserver.BaseRequestHandler):
    _id = 100

    def _nextGlobalId(self):
        next_id = self._id
        self._id += 1
        return next_id

    def _handle_init(self, data):
        print('Handling adapter config')
        global_id = data['globalId']
        db_key = {'globalId': global_id}

        print('Connecting to db...')
        client = MongoClient()
        db = client[DB_NAME]
        coll = db[COLL_NAME]

        cur = coll.find_one(db_key)

        print('Current saved client state: ' + str(cur))

        update = {'$set': {}}

        for key in data:
            update['$set'][key] = data[key]

        print('Update client state: ' + str(update))

        coll.update(db_key, update)

        auto_data = coll.find_one(db_key)
        del auto_data['_id']
        autodeploy(auto_data)

        client.close()

    def _handle_hello(self, data):
        print('Handling hello message')
        local_id = data[const.LOCAL_ID]
        dev_ip = data[const.DEV_IP]

        global_id = self._nextGlobalId()

        reply = {
            const.DEV_IP: dev_ip,
            const.LOCAL_ID: local_id,
            const.GLOBAL_ID: global_id
        }

        print('Connecting to db...')
        client = MongoClient()
        db = client[DB_NAME]
        coll = db[COLL_NAME]

        db_entry = {
            const.GLOBAL_ID: global_id,
            const.DEV_IP: dev_ip,
            const.LOCAL_ID: local_id,
            const.TYPE: data[const.TYPE],
        }

        print('Saving: ' + str(db_entry))

        coll.insert_one(
            db_entry
        )

        client.close()

        socket = self.request[1]
        socket.sendto(json.dumps(reply).encode('utf-8'), self.client_address)

    def _handle_ping(self, data):
        if const.PING_MSG in data and data[const.PING_MSG] == 'ping':
            data[const.PING_MSG] = 'pong'
        socket = self.request[1]
        socket.sendto(json.dumps(data).encode('utf-8'), self.client_address)

    def _handle_value(self, data):
        pass

    def _handle_keepalive(self, data):
        pass

    def handle(self):
        data = self.request[0].decode('utf-8')
        print('Received: ' + str(data))

        data = json.loads(data)

        connection_types = {
            const.CONN_HELLO: self._handle_hello,
            const.CONN_INIT: self._handle_init,
            const.CONN_VALUE: self._handle_value,
            const.CONN_KEEP_ALIVE: self._handle_keepalive,
            const.CONN_PING: self.handle_ping,

        }

        if const.CONN_TYPE in data:
            conn_type = data[const.CONN_TYPE]
            if conn_type in connection_types:
                connection_types[conn_type]()
            else:
                raise NotImplementedError('Unknown connection type |' + conn_type + '|')


# main
if __name__ == "__main__":
    print('Start')

    # bind socket for broadcasts
    server = socketserver.UDPServer(('', PORT), ConndeHandler)
    print('Waiting for messages...')
    server.serve_forever()








    # s = sck.socket(sck.AF_INET, sck.SOCK_DGRAM)
    # s.bind(('', PORT))
    #
    # # access db
    # # client = MongoClient()
    # # db = client[DB_NAME]
    # # coll = db[COLL_NAME]c
    # # result = coll.create_index([('macAddress', pymongo.ASCENDING)], unique=True)
    #
    # print('Waiting for messages...')
    # # loop n wait for messages
    # while (True):
    #     message, (addr, port) = s.recvfrom(1024)
    #
    #     # starts a thread and waits for next message
    #     # t = Thread(target=proccessMessage, args=(coll, message))
    #     t = Thread(target=proccessMessage, args=(None, message, (addr, port)))
    #     t.start()
