import datetime
import json
import netifaces as ni
import socket as sck

import zeroconf as zerocnf

from advertiseservice.advertiserclient import *


@AdvertiserClient.register
class LanAdvertiser(AdvertiserClient):
    def __init__(self, service, comm_type=const.LAN):
        AdvertiserClient.__init__(self, service, comm_type)
        self.s = sck.socket(sck.AF_INET, sck.SOCK_DGRAM)
        self.s.setsockopt(sck.SOL_SOCKET, sck.SO_BROADCAST, 1)
        self.s.settimeout(const.CLIENT_TIMEOUT)

        self.discovered_server = None

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

        log.info("Starting DNS-SD discovery")
        dnssd = zerocnf.Zeroconf()
        service_browser = zerocnf.ServiceBrowser(dnssd, const.DNS_SD_TYPE + const.DNS_SD_LOCAL_DOMAIN,
                                                 handlers=[self.server_discovered])
        discovery_start = datetime.datetime.utcnow()
        while self.discovered_server is None:
            now = datetime.datetime.utcnow()
            if now - discovery_start > datetime.timedelta(seconds=5):  # wait for max 5 seconds
                break

            time.sleep(0.5)

        service_browser.cancel()
        dnssd.close()
        if self.discovered_server is not None:
            return self.discovered_server

        # fallback broadcast
        log.warning("Could not find servier using DNS-SD. Using broadcast as fallback")

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
                            result = self._ping_server(bc_addr)
                        else:
                            log.info('connecting to hardcoded ip |127.0.0.1|')
                            result = self._ping_server('127.0.0.1')

                        if result:
                            return result, ip, mac

        return False

    def _ping_server(self, target_address, target_port=const.PORT):
        data = {
            const.CONN_TYPE: const.CONN_PING,
            const.PING_MSG: 'ping'
        }

        msg = json.dumps(data).encode('utf-8')
        log.debug('Ping server with |' + msg.decode('utf-8') + '|')
        self.s.sendto(msg, (target_address, target_port))

        try:
            msg, srv_addr = self.s.recvfrom(1024)
            log.debug('Received pong |' + msg.decode('utf-8') + '|')
            data = json.loads(msg.decode('utf-8'))
            if const.PING_MSG in data and data[const.PING_MSG] == 'pong':
                log.info('server found at |' + str(srv_addr) + '|')
                server_address = srv_addr
                return server_address
        except sck.timeout:
            log.debug('No response')

    def server_discovered(self, zeroconf, service_type, name, state_change):
        """

        :param zeroconf:
        :type zeroconf: zerocnf.Zeroconf
        :param service_type:
        :type service_type: str
        :param name:
        :type name: str
        :param state_change:
        :type state_change: zerocnf.ServiceStateChange
        :return:
        """
        log.debug("Service %s of type %s state changed: %s", name, service_type, state_change)

        if state_change is zerocnf.ServiceStateChange.Added:
            info = zeroconf.get_service_info(service_type, name)
            if info:
                service_addr = sck.inet_ntoa(info.address)
                service_port = info.port
                log.info("Discovered server |%s| at |%s:%s| using DNS-SD", name, str(service_addr), str(service_port))
                ip, mac = self._read_own_address(service_addr, service_port)
                if ip:
                    self.discovered_server = ((service_addr, service_port), ip, mac)

    def _read_own_address(self, target_addr, target_port=const.PORT):
        interfaces = ni.interfaces()
        for i in interfaces:  # interface
            if ni.AF_INET in ni.ifaddresses(i):  # if interface has AF_INET (IPv4)
                addrs = ni.ifaddresses(i)[ni.AF_INET]
                mac_addrs = ni.ifaddresses(i)[ni.AF_LINK]
                for addr in addrs:  # list of IPv4 addresses (yes, there's more than one)
                    for mac_addr in mac_addrs:
                        ip = addr['addr']
                        mac = mac_addr['addr']
                        result = self._ping_server(target_addr, target_port)

                        if result:
                            return ip, mac
