import json

import bluetooth

from .advertiser import *


class BTAdvertiser(Advertiser):
    def __init__(self, service, comm_type=const.BT):
        Advertiser.__init__(self, service, comm_type)
        self.client_sck = bluetooth.BluetoothSocket(bluetooth.RFCOMM)

    def discover_service(self):
        """
        Discover the discovery service using Bluetooth SDP.
        :return: tuple containing the server address, the local ip and the local hardware address.
        :rtype: tuple
        """
        service_matches = bluetooth.find_service(uuid=const.BT_UUID,
                                                 address=None)  # search all nearby devices for service

        if len(service_matches) == 0:
            log.info('No matching service found')
            return False

        for match in service_matches:
            port = match['port']
            name = match['name']
            host = match['host']

            log.info('Found matching service to |%s| on |%s|', name, host)

            srv_addr = (host, port)
            try:
                self.client_sck.connect(srv_addr)
                self.client_sck.settimeout(const.CLIENT_TIMEOUT)

                ping_msg = {
                    const.CONN_TYPE: const.CONN_PING,
                    const.PING_MSG: 'ping'
                }

                self._send_msg(ping_msg)

                ping_reply = self._receive_msg()

                if ping_reply and const.PING_MSG in ping_reply and ping_reply[const.PING_MSG] == 'pong':
                    log.info('service found at |' + str(srv_addr) + '|')
                    return srv_addr, None, self.client_sck.getsockname()[0]
            except bluetooth.BluetoothError as bt_err:
                error_msg = str(bt_err)
                if not error_msg.__eq__('timed out'):
                    log.exception('Error connecting to service')

        return False

    def _receive_msg(self):
        """
        Receive a message from the service.
        As RFCOMM maintains a connection, the data arrives as a byte stream.
        It is not specified how many bytes are returned from the stream in every read operation.
        Thus, it is not guaranteed that each read operation returns exactly one message.
        As a consequence, a read operation can return no, one or more messages.

        After every read operation on the stream, check if the returned data can be parsed as a json object.
        If this fails read again until a valid json object could be parsed.
        If the object was parsed successfully, the read bytes may contain more bytes which belong to the next message.
        Return the parsed object and store the remainder for the next message to be returned.
        :return: the received message
        :rtype: dict
        """
        msg = ''
        while True:
            try:
                data = self.client_sck.recv(1024)
                data = data.decode(const.ENCODING)
            except bluetooth.BluetoothError as bt_err:
                error_msg = str(bt_err)
                if not error_msg.__eq__('timed out'):
                    raise bt_err

            if not data:  # on an empty string the connection was closed
                return False

            msg += data

            try:
                json_data = json.loads(msg)
                if hasattr(self.client_sck, 'getpeername'):
                    log.debug('Recieved message |%s| from |%s|', msg, str(self.client_sck.getpeername()))
                else:
                    log.debug('Received message |%s|', msg)
                return json_data
            except json.JSONDecodeError:
                log.debug('Could not load JSON object from |%s|', msg)

    def _send_msg(self, msg):
        msg_string = json.dumps(msg)
        if hasattr(self.client_sck, 'getpeername'):
            log.debug('Sending message |' + msg_string + '| to |' + str(self.client_sck.getpeername()) + '|')
        else:
            log.debug('Sending message |%s|', msg_string)

        if hasattr(self.client_sck, 'sendall'):
            self.client_sck.sendall(msg_string.encode(const.ENCODING))
        else:
            to_send = len(msg_string)
            sent = 0
            while sent < to_send:
                remaining_msg = msg_string[sent:to_send]
                sent += self.client_sck.send(remaining_msg.encode(const.ENCODING))
