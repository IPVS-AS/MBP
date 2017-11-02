import json
import socket
import socketserver

from zeroconf import Zeroconf, ServiceInfo

from discovery.discoverygateway import *


@ConndeHandler.register
class ConndeLanHandler(socketserver.BaseRequestHandler, ConndeHandler):
    def __init__(self, request, client_address, server):
        socketserver.BaseRequestHandler.__init__(self, request, client_address, server)
        ConndeHandler.__init__(self, server)

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


@ConndeGateway.register
class ConndeLanGateway(socketserver.UDPServer, ConndeGateway):
    def __init__(self, RequestHandlerClass, db_client, service, server_address):
        ConndeGateway.__init__(self, const.LAN, db_client, service)
        socketserver.UDPServer.__init__(self, server_address=server_address, RequestHandlerClass=RequestHandlerClass)
        self.service_info = ServiceInfo(const.DNS_SD_TYPE + const.DNS_SD_LOCAL_DOMAIN,
                                        "Your Lan Gateway." + const.DNS_SD_TYPE + const.DNS_SD_LOCAL_DOMAIN,
                                        socket.inet_aton("127.0.0.1"), const.PORT, 0, 0,{const.GLOBAL_ID: const.CONNDE_DB_NAME})
        self.zeroconf = Zeroconf()
        self.zeroconf.register_service(self.service_info)

    def shutdown(self):
        self.zeroconf.unregister_service(self.service_info)
        self.zeroconf.close()
        socketserver.UDPServer.shutdown(self)

    def deploy_adapter(self, service_file, routines):
        pass
