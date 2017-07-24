import json
from advertiserclient import *
import bluetooth


class BTAdvertiser(AdvertiserClient):
    def __init__(self, service, comm_type=const.BT):
        AdvertiserClient.__init__(self, service, comm_type)
        self.client_sck = bluetooth.BluetoothSocket(bluetooth.RFCOMM)
        self.client_sck.settimeout(const.CLIENT_TIMEOUT)

    def discover_server(self):
        service_matches = bluetooth.find_service(uuid=const.BT_UUID,
                                                 address=None)  # search all nearby devices for server

        if len(service_matches) == 0:
            return False

        for match in service_matches:
            port = match['port']
            name = match['name']
            host = match['host']

            log.info('Found matching service to |%s| on |%s|', name, host)

            srv_addr = (host, port)
            self.client_sck.connect(srv_addr)

            ping_msg = {
                const.CONN_TYPE: const.CONN_PING,
                const.PING_MSG: 'ping'
            }

            self._send_msg(ping_msg)
            try:
                ping_reply = self._receive_msg()

                if const.PING_MSG in ping_reply and ping_reply[const.PING_MSG] == 'pong':
                    log.info('server found at |' + str(srv_addr) + '|')
                    self.server_address = srv_addr
                    return True
            except bluetooth.BluetoothError as bt_err:
                error_msg = str(bt_err)
                if not error_msg.__eq__('timed out'):
                    raise bt_err

    def _receive_msg(self):
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
                log.debug('Recieved message |%s| from |%s|', msg, self.client_sck.getpeername())
                return json_data
            except json.JSONDecodeError:
                log.debug('Could not load JSON object from |%s|', msg)

    def _send_msg(self, msg):
        msg_string = json.dumps(msg)
        log.debug('Sending message |' + msg_string + '| to |' + self.client_sck.getpeername() + '|')
        self.client_sck.sendall(msg_string)
