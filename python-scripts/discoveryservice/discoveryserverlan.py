import json
import socketserver

from discoveryservice.discoveryserver import *


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





@ConndeServer.register
class ConndeLanServer(socketserver.UDPServer, ConndeServer):
    def __init__(self, RequestHandlerClass, db_client, service, server_address):
        ConndeServer.__init__(self, const.LAN, db_client, service)
        socketserver.UDPServer.__init__(self, server_address=server_address, RequestHandlerClass=RequestHandlerClass)