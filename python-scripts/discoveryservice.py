import threading
import logging as log
import time
import datetime
import json
import requests
import const

from pymongo import MongoClient
from discoveryserverbt import *
from discoveryserverlan import *

PORT = 20123

DB_NAME = 'discovery'
DEV_COLL_NAME = 'devices'
STATUS_COLL_NAME = 'status'
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

    return e1[const.DEV_IP] == e2[const.DEV_IP] and \
           e1[const.DEV_HW_ADDRESS] == e2[const.DEV_HW_ADDRESS] and \
           e1[const.LOCAL_ID] == e2[const.LOCAL_ID] and \
           e1[const.DEV_TYPE] == e2[const.DEV_TYPE]


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
            autodeploy(data)


class DiscoveryService:
    def __init__(self):
        self.db_client = MongoClient()
        db = self.db_client[DB_NAME]

        self.status_coll = db[STATUS_COLL_NAME]
        init_status = self.status_coll.find_one({const.STATUS_FOR: const.SERVER_SERVICE})
        if const.STATUS_NEXT_ID in init_status:
            self._next_id = int(init_status[const.STATUS_NEXT_ID])
            log.info('restored init status with next_id=|%d|', self._next_id)
        else:
            log.warning('no valid init status found')
            self._next_id = 100

        self.id_lock = threading.Lock()

        self.servers = []
        self.serverthreads = dict()

    def next_global_id(self):
        self.id_lock.acquire()
        tmp = self._next_id
        self._next_id += 1
        self.id_lock.release()
        return tmp

    def start(self, lan=True, bt=True):
        log.info('Starting discovery service...')

        if lan:
            log.info('Starting LAN...')
            lan_server = ConndeLanServer(ConndeLanHandler, self.db_client, self, ('', PORT))
            lan_thread = threading.Thread(target=lan_server.serve_forever)
            lan_thread.start()
            self.servers.append(lan_server)
            self.serverthreads[lan_server.comm_type] = lan_thread

        if bt:
            log.info('Starting BT...')
            bt_server = ConndeBluetoothServer(ConndeBluetoothHandler, self.db_client, self)
            bt_thread = threading.Thread(target=bt_server.serve_forever)
            bt_thread.start()
            self.servers.append(bt_server)
            self.serverthreads[bt_server.comm_type] = bt_thread

        log.info('Waiting for messages...')

    def stop(self):
        log.info('Stopping discovery service')

        for server in self.servers:
            log.info('Shutting down %s...', server.comm_type)
            server.shutdown()
            self.serverthreads[server.comm_type].join()

        log.info('saving service status...')
        self.status_coll.update_one({const.STATUS_FOR: const.SERVER_SERVICE},
                                    {
                                        '$set': {const.STATUS_NEXT_ID: self._next_id}
                                    })
        self.db_client.close()


# main
if __name__ == "__main__":
    log.basicConfig(format='%(asctime)s |%(levelname)s|:%(message)s', level=log.DEBUG)
    
    discovery_service = DiscoveryService()

    # bind socket for broadcasts
    try:
        discovery_service.start()
        while True:
            print('.', end='', flush=True)
            time.sleep(5)
    except KeyboardInterrupt:
        log.info('Keyboard interrupt. Shutting down...')
        discovery_service.stop()
