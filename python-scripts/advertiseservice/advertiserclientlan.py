import json
import netifaces as ni
import socket as sck

from advertiseservice.advertiserclient import *


@AdvertiserClient.register
class LanAdvertiser(AdvertiserClient):
    def __init__(self, service, comm_type=const.LAN):
        AdvertiserClient.__init__(self, service, comm_type)
        self.s = sck.socket(sck.AF_INET, sck.SOCK_DGRAM)
        self.s.setsockopt(sck.SOL_SOCKET, sck.SO_BROADCAST, 1)
        self.s.settimeout(const.CLIENT_TIMEOUT)

    def _receive_msg(self):
        try:
            data, srv_addr = self.s.recvfrom(1024)

            msg_string = data.decode(const.ENCODING)
            log.debug('Recieved message |%s| from |%s|', msg_string, str(srv_addr))

            msg = json.loads(msg_string)
            return msg

        except json.JSONDecodeError:
            log.exception('Error loading json')
            return False
        except sck.timeout:
            log.exception('No response')
            return False

    def _send_msg(self, msg):
        if self.server_address is None:
            raise ValueError('server_address is None')

        msg_string = json.dumps(msg)
        log.debug('Sending message |%s| to |%s|', msg_string, str(self.server_address))

        self.s.sendto(msg_string.encode('utf-8'), self.server_address)

    def discover_server(self):
        log.info('Discovering server')
        interfaces = ni.interfaces()
        for i in interfaces:  # interface
            if ni.AF_INET in ni.ifaddresses(i):  # if interface has AF_INET (IPv4)
                addrs = ni.ifaddresses(i)[ni.AF_INET]
                mac_addrs = ni.ifaddresses(i)[ni.AF_LINK]
                for addr in addrs:  # list of IPv4 addresses (yes, there's more than one)
                    for mac_addr in mac_addrs:
                        ip = addr['addr']
                        bc_addr = False
                        if 'broadcast' in addr and 'addr' in addr:  # if it has a broadcast address
                            bc_addr = addr['broadcast']

                        # if ip == "127.0.0.1":  # omit the loop interface
                        #     continue

                        mac = mac_addr['addr']

                        if bc_addr:
                            log.info('broadcasting to: |' + str(bc_addr) + '|')
                            result = self._ping_server(ip, mac, bc_addr)
                        else:
                            log.info('connecting to hardcoded ip |127.0.0.1|')
                            result = self._ping_server(ip, mac, '127.0.0.1')

                        if result:
                            return result


        return False

    def _ping_server(self, ip, mac, target_address):
        data = {
            const.CONN_TYPE: const.CONN_PING,
            const.PING_MSG: 'ping'
        }

        msg = json.dumps(data).encode('utf-8')
        print('Ping server with |' + msg.decode('utf-8') + '|')
        self.s.sendto(msg, (target_address, const.PORT))

        try:
            msg, srv_addr = self.s.recvfrom(1024)
            print('Received pong |' + msg.decode('utf-8') + '|')
            data = json.loads(msg.decode('utf-8'))
            if const.PING_MSG in data and data[const.PING_MSG] == 'pong':
                print('server found at |' + str(srv_addr) + '|')
                server_address = srv_addr
                return server_address, ip, mac
        except sck.timeout:
            print('No response')