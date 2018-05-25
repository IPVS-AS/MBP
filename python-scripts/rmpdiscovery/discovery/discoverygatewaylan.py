import json
import socket
import socketserver

from zeroconf import Zeroconf, ServiceInfo

from .discoverygateway import *


@RMPHandler.register
class RMPLanHandler(socketserver.BaseRequestHandler, RMPHandler):
    def __init__(self, request, client_address, gateway):
        RMPHandler.__init__(self, gateway)
        socketserver.BaseRequestHandler.__init__(self, request, client_address, gateway)

    def _receive_msg(self):
        msg_string = self.request[0].decode('utf-8')
        log.debug('Received |' + msg_string + '| from |' + str(self.client_address) + '|')

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


@DiscoveryGateway.register
class DiscoveryLanGateway(socketserver.UDPServer, DiscoveryGateway):
    """
    The Gateway used in IP-based networks.
    Uses the connectionless UDP protocol and extends the UDPServer class.
    """
    def __init__(self, service):
        DiscoveryGateway.__init__(self, const.LAN, service)
        socketserver.UDPServer.__init__(self, ('', const.PORT), RequestHandlerClass=RMPLanHandler)
        # advertise using dns-sd
        self.service_info = ServiceInfo(const.DNS_SD_TYPE + const.DNS_SD_LOCAL_DOMAIN,
                                        "Your Lan Gateway." + const.DNS_SD_TYPE + const.DNS_SD_LOCAL_DOMAIN,
                                        socket.inet_aton("127.0.0.1"), const.PORT, 0, 0, {const.GLOBAL_ID: const.RMP_DB_NAME})
        self.zeroconf = Zeroconf()
        self.zeroconf.register_service(self.service_info)

    def shutdown(self):
        self.zeroconf.unregister_service(self.service_info)
        self.zeroconf.close()
        socketserver.UDPServer.shutdown(self)
