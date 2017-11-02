import abc
import logging as log

import discovery.discoveryconst as const


class ServiceAdapter(abc.ABC):
    @abc.abstractmethod
    def connect_new_device(self, device):
        return 0, 'not implemented'

    @abc.abstractmethod
    def reconnect_device(self, device):
        return 0, 'not implemented'

    @abc.abstractmethod
    def save_init(self, dev, init):
        pass

    @abc.abstractmethod
    def device_alive(self, device):
        pass

    @abc.abstractmethod
    def getConndeId(self, global_id):
        pass


class DiscoveryGateway(abc.ABC):
    def __init__(self, comm_type, db_client, service):
        """

        :param comm_type:
        :param db_client:
        :param service:
        :type service: ServiceAdapter
        """
        self.comm_type = comm_type
        self.service = service
        self.db_client = db_client
        self.dev_coll = self.db_client[const.DISCOVERY_DB_NAME][const.DEV_COLL_NAME]
        self.mon_coll = self.db_client[const.DISCOVERY_DB_NAME][const.MONITOR_COLL_NAME]
        self.status_coll = self.db_client[const.DISCOVERY_DB_NAME][const.STATUS_COLL_NAME]

    @abc.abstractmethod
    def serve_forever(self):
        pass

    @abc.abstractmethod
    def shutdown(self):
        pass

    @abc.abstractmethod
    def deploy_adapter(self, service_file, routines):
        pass


class ConndeHandler(abc.ABC):
    def __init__(self, server):
        """

        :param server:
        :type server: DiscoveryGateway
        """
        self.server = server

    @abc.abstractmethod
    def _send_msg(self, msg):
        pass

    @abc.abstractmethod
    def _receive_msg(self):
        pass

    @abc.abstractmethod
    def handle(self):
        pass

    def _handle_hello(self, data):
        local_id = data[const.LOCAL_ID]
        dev_ip = data[const.DEV_IP]
        dev_hw_addr = data[const.DEV_HW_ADDRESS]

        if const.GLOBAL_ID in data:
            global_id, error_message = self.server.service.reconnect_device(data)
        else:
            global_id, error_message = self.server.service.connect_new_device(data)

        if global_id:
            reply = {
                const.DEV_IP: dev_ip,
                const.LOCAL_ID: local_id,
                const.DEV_HW_ADDRESS: dev_hw_addr,
                const.GLOBAL_ID: global_id
            }

        else:
            reply = {
                const.GLOBAL_ID: 0,
                const.DEV_IP: dev_ip,
                const.LOCAL_ID: local_id,
                const.DEV_HW_ADDRESS: dev_hw_addr,
                const.ERROR: error_message
            }

        self._send_msg(reply)

    def _handle_conf(self, data):
        global_id = data[const.GLOBAL_ID]
        device = {const.GLOBAL_ID: global_id}

        init = dict()

        for key in data:
            if key not in [const.GLOBAL_ID, const.CONN_TYPE]:
                init[key] = data[key]

        self.server.service.save_init(device, init)
        connde_id = self.server.service.getConndeId(global_id)
        self._send_msg({
            const.GLOBAL_ID: global_id,
            const.CONNDE_ID: connde_id  # hack to deploy android device
        })  # ACK TODO let the system set the timeout

    def _handle_ping(self, data):
        if const.PING_MSG in data and data[const.PING_MSG] == 'ping':
            data[const.PING_MSG] = 'pong'

        self._send_msg(data)

    def _handle_value(self, data):
        pass

    def _handle_keepalive(self, data):
        global_id = data[const.GLOBAL_ID]
        self.server.service.device_alive({const.GLOBAL_ID: global_id})

    def _handle_msg(self, msg):
        connection_types = {
            const.CONN_HELLO: self._handle_hello,
            const.CONN_CONF: self._handle_conf,
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
