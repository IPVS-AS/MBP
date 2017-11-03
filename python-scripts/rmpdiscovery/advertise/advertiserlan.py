import datetime
import json
import netifaces as ni
import socket as sck

import zeroconf as zerocnf

from .advertiser import *


@Advertiser.register
class LanAdvertiser(Advertiser):
    """
    Advertiser for IP-based networks.
    Discovers the service using DNS-SD or plain Broadcasts as fallback.
    """
    def __init__(self, service):
        """
        Initzialize the Advertiser and its sockets
        :param service:
        """
        Advertiser.__init__(self, service, const.LAN)
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

    def discover_service(self):
        """
        Discover the discovery service using DNS-SD.
        If DNS-SD does not discover a server after 5 seconds, try plain broadasts as fallback.
        :return: tuple containing the server address, the local ip and the local hardware address.
        :rtype: tuple
        """
        log.info('Discovering service')

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

        service_browser.cancel()  # stop browsing services
        dnssd.close()
        if self.discovered_server is not None:
            return self.discovered_server

        # fallback broadcast
        log.warning("Could not find service using DNS-SD. Using broadcast as fallback")

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
        """
        Ping the service using the given address and port.
        :param target_address: address of the service or broadcast address
        :param target_port: port of the service
        :return: the services address
        :rtype: tuple
        """
        data = {
            const.CONN_TYPE: const.CONN_PING,
            const.PING_MSG: 'ping'
        }

        msg = json.dumps(data).encode('utf-8')
        log.debug('Ping service with |' + msg.decode('utf-8') + '|')
        self.s.sendto(msg, (target_address, target_port))

        try:
            msg, srv_addr = self.s.recvfrom(1024)
            log.debug('Received pong |' + msg.decode('utf-8') + '|')
            data = json.loads(msg.decode('utf-8'))
            if const.PING_MSG in data and data[const.PING_MSG] == 'pong':
                log.info('service found at |' + str(srv_addr) + '|')
                server_address = srv_addr
                return server_address
        except sck.timeout:
            log.debug('No response')

    def server_discovered(self, zeroconf, service_type, name, state_change):
        """
        Store the discovered server.

        :type zeroconf: zerocnf.Zeroconf
        :param service_type: type of the discovered service
        :type service_type: str
        :param name: name of the discovered service
        :type name: str
        :param state_change: has service been ADDED or REMOVED?
        :type state_change: zerocnf.ServiceStateChange
        """
        log.debug("Service %s of type %s state changed: %s", name, service_type, state_change)

        if state_change is zerocnf.ServiceStateChange.Added:
            info = zeroconf.get_service_info(service_type, name)
            if info:
                service_addr = sck.inet_ntoa(info.address)
                service_port = info.port
                log.info("Discovered service |%s| at |%s:%s| using DNS-SD", name, str(service_addr), str(service_port))
                ip, mac = self._read_own_address(service_addr, service_port)
                if ip:
                    self.discovered_server = ((service_addr, service_port), ip, mac)

    def _read_own_address(self, target_addr, target_port=const.PORT):
        """
        Zeroconf does not provide a way to read the interface on which it has discovered a service.
        Thus, the interface, with it the IP and MAC, must be found using pings to the service.

        :param target_addr: IP of the service
        :param target_port: port of the service
        :return: tuple containing the local IP and local MAC
        :rtype: tuple
        """
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
