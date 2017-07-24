import threading
import logging as log
import time
import abc
import datetime
import socketserver
import socket as sck
import json
import requests
import const
import bluetooth

from pymongo import MongoClient

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
           e1[const.DEV_MAC] == e2[const.DEV_MAC] and \
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


def next_global_id():
    global _id
    next_id = _id
    _id += 1
    return next_id


def server_close():
    log.info('saving server status...')
    status_coll.update_one({'init_status': 'true'},
                           {
                               '$set': {const.SERVER_NEXT_ID: _id}
                           })
    global_db_client.close()

class ConndeHandler(abc.ABC):
    def __init__(self, server):
        self.server = server

    @abc.abstractmethod
    def _send_msg(self, msg):
        pass

    @abc.abstractmethod
    def _receive_msg(self):
        pass

    def _handle_hello(self, data):
        log.debug('Handling hello message')

        local_id = data[const.LOCAL_ID]
        dev_ip = data[const.DEV_IP]
        dev_type = data[const.DEV_TYPE]

        if const.GLOBAL_ID in data:
            log.info('Device reentering envbironment')
            global_id = data[const.GLOBAL_ID]
        else:
            log.info('New Device discovered')

            global_id = next_global_id()

        reply = {
            const.DEV_IP: dev_ip,
            const.LOCAL_ID: local_id,
            const.GLOBAL_ID: global_id
        }

        db_key = {
            const.GLOBAL_ID: global_id
        }

        db_entry = {
            '$set': {
                const.DEV_IP: dev_ip,
                const.LOCAL_ID: local_id,
                const.TYPE: dev_type
            },
            '$currentDate': {
                const.LAST_CONTACT: True
            }
        }

        log.debug('Saving: ' + str(db_entry) + ' for key: ' + str(db_key))

        self.server.db_coll.update_one(
            db_key,
            db_entry,
            upsert=True

        self._send_msg(reply)

    def _handle_init(self, data):
        log.debug('Handling adapter config')
        global_id = data[const.GLOBAL_ID]
        db_key = {const.GLOBAL_ID: global_id}

        cur = self.server.db_coll.find_one(db_key)

        log.debug('Current saved client state: ' + str(cur))

        update = {
            '$set': {
                const.ADAPTER_CONF: {}
            },
            '$currentDate': {
                const.LAST_CONTACT: True
            }
        }

        for key in data:
            if key not in [const.GLOBAL_ID, const.CONN_TYPE]:
                update['$set'][const.ADAPTER_CONF][key] = data[key]

        log.debug('Update client state: ' + str(update))

        self.server.db_coll.update_one(db_key, update)

        # auto_data = self.server.db_coll.find_one(db_key)
        # del auto_data['_id']
        # autodeploy(auto_data)

    def _handle_ping(self, data):
        log.debug('handle ping request')
        if const.PING_MSG in data and data[const.PING_MSG] == 'ping':
            data[const.PING_MSG] = 'pong'

        self._send_msg(data)

    def _handle_value(self, data):
        pass

    def _handle_keepalive(self, data):
        pass

    def _handle_msg(self, msg):
        connection_types = {
            const.CONN_HELLO: self._handle_hello,
            const.CONN_INIT: self._handle_init,
            const.CONN_VALUE: self._handle_value,
            const.CONN_KEEP_ALIVE: self._handle_keepalive,
            const.CONN_PING: self._handle_ping,

        }

        if const.CONN_TYPE in msg:
            conn_type = msg[const.CONN_TYPE]
            if conn_type in connection_types:
                try:
                    del msg[const.CONN_TYPE]
                    connection_types[conn_type](msg)
                except BaseException as ex:
                    log.exception('Error during handling |' + conn_type + '|')
            else:
                raise NotImplementedError('Unknown connection type |' + conn_type + '|')


@ConndeHandler.register
class ConndeLanHandler(socketserver.BaseRequestHandler, ConndeHandler):
    def __init__(self, request, client_address, server):
        socketserver.BaseRequestHandler.__init__(self, request, client_address, server)
        ConndeHandler.__init__(self, server)

    def _receive_msg(self):
        msg_string = self.request[0].decode('utf-8')
        log.debug('Received |' + msg_string + '| from |' + str(self.client_address) + '|')
        print('Received: ' + msg_string)

        try:
            msg = json.loads(msg_string)
            return msg
        except json.JSONDecodeError as err:
            log.exception('Invalid message format')
            return False

    def _send_msg(self, msg):
        msg_string = json.dumps(msg)
        log.debug('Sending message |' + msg_string + '| to |' + str(self.client_address) + '|')

        socket = self.request[1]
        socket.sendto(msg_string.encode('utf-8'), self.client_address)

    def handle(self):
        msg = self._receive_msg()
        self._handle_msg(msg)


<<<<<<< HEAD
class ConndeServer(abc.ABC):
    def __init__(self, db_client):
        self.db_client = db_client
        self.db_coll = self.db_client[DB_NAME][DEV_COLL_NAME]
        self.status_coll = self.db_client[DB_NAME][STATUS_COLL_NAME]

    @abc.abstractmethod
    def serve_forever(self):
        pass

    @abc.abstractmethod
    def shutdown(self):
        pass


@ConndeServer.register
class ConndeLanServer(socketserver.UDPServer, ConndeServer):
    def __init__(self, RequestHandlerClass, db_client, server_address):
        ConndeServer.__init__(self, db_client)
        socketserver.UDPServer.__init__(self, server_address=server_address, RequestHandlerClass=RequestHandlerClass)


@ConndeHandler.register
class ConndeBluetoothHandler(ConndeHandler):
    def __init__(self, server, client_sck, client_info):
        ConndeHandler.__init__(self, server)
        self.client_sck = client_sck
        self.client_info = client_info

    def handle(self):
        log.info('Handling new connection to |' + self.client_info + '|')
        log.info('Connection timeout is |' + self.client_sck.gettimeout())

        msg = self._receive_msg()
        while msg:  # as long there are messages handle them
            self._handle_msg(msg)

        # when no messages arrive, close the connection
        self.client_sck.close()

    def _receive_msg(self):
        log.debug('Trying to receive msg from |' + self.client_info + '|')
        msg = ''
        while True:
            try:
                data = self.client_sck.recv(1024)
            except bluetooth.BluetoothError as bt_err:
                error_msg = str(bt_err)
                if not error_msg.__eq__('timed out'):
                    raise bt_err

            if not data:  # on an empty string the connection was closed
                return False

            msg += data

            try:
                json_data = json.loads(msg)
                self._handle_msg(json_data)
                msg = ''
            except json.JSONDecodeError:
                log.debug('Could not load JSON object from |%s|', msg)

    def _send_msg(self, msg):
        msg_string = json.dumps(msg)
        log.debug('Sending message |' + msg_string + '| to |' + self.client_info + '|')
        self.client_sck.sendall(msg_string)


@ConndeServer.register
class ConndeBluetoothServer(ConndeServer):
    def __init__(self, RequestHandlerClass, db_client, poll_interval=0.5):
        ConndeServer.__init__(self, db_client)
        self.RequestHandlerClass = RequestHandlerClass
        self.server_sck = bluetooth.BluetoothSocket()
        self.server_sck.settimeout(poll_interval)
        self.server_sck.bind(('', bluetooth.PORT_ANY))
        self.server_sck.listen(1)
        self._shutdown = False
        log.info('Started BT socket on RFCOMM channel %d', self.server_sck.getsockname()[1])
        bluetooth.advertise_service(self.server_sck,
                                    const.BT_SERVICE_NAME,
                                    service_id=const.BT_UUID,
                                    service_classes=[const.BT_UUID, bluetooth.SERIAL_PORT_CLASS],
                                    profiles=[bluetooth.SERIAL_PORT_PROFILE],
                                    description=const.BT_SERVICE_DESCRIPTION,
                                    protocols=[bluetooth.RFCOMM_UUID]
                                    )
        log.info('Advertising BT service')

    def serve_forever(self):
        while not self._shutdown:
            try:
                client_sck, client_info = self.server_sck.accept()
                self.RequestHandlerClass(client_sck, client_info)
                client_sck.close()
            except bluetooth.BluetoothError as bt_err:
                error_msg = str(bt_err)
                if not error_msg.__eq__('timed out'):
                    raise bt_err

        bluetooth.stop_advertising(self.server_sck)
        self.server_sck.close()

    def shutdown(self):
        self._shutdown = True


# main
if __name__ == "__main__":
    log.basicConfig(format='%(asctime)s |%(levelname)s|:%(message)s', level=log.DEBUG)
    log.info('Start')

    global_db_client = MongoClient()
    db = global_db_client[DB_NAME]

    status_coll = db[STATUS_COLL_NAME]
    init_status = status_coll.find_one({'init_status': 'true'})
    if const.SERVER_NEXT_ID in init_status:
        _id = int(init_status[const.SERVER_NEXT_ID])
        log.info('restored init status with next_id=|%d|', _id)
    else:
        log.warning('no valid init status found')
        _id = 100

    # bind socket for broadcasts
    lan_server = ConndeLanServer(ConndeLanHandler, global_db_client, ('', PORT))
    bt_server = ConndeBluetoothServer(ConndeBluetoothHandler, global_db_client)
    lan_thread = threading.Thread(target=lan_server.serve_forever)
    bt_thread = threading.Thread(target=bt_server.serve_forever)
    try:
        log.info('Starting LAN...')
        lan_thread.start()
        log.info('Starting BT...')
        bt_thread.start()
        log.info('Waiting for messages')
        while True:
            print('.')
            time.sleep(5)
    except KeyboardInterrupt:
        log.info('Keyboard interrupt. Shutting down...')
        log.info('Shutting down LAN...')
        lan_server.shutdown()
        log.info('Shutting down BT...')
        bt_server.shutdown()
        log.info('Waiting for LAN...')
        lan_thread.join()
        log.info('Waiting for BT...')
        bt_thread.join()
        server_close()
