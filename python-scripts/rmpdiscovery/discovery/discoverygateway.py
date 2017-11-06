import abc
import logging as log

from . import discoveryconst as const


class ServiceAdapter(abc.ABC):
    @abc.abstractmethod
    def connect_new_device(self, device):
        """
        Connect a new device with the RMP.

        The device is specified by a dictionary containing information necessary for registration:
            {
                const.DEV_TYPE: type,
                const.LOCAL_ID: local_id,
                const.DEV_HW_ADDRESS: hw_addr,
                const.DEV_IP: ip, (optional)
                const.HOST: host (optional)
            }

        Check for a duplicate in the system.
        In case a duplicate was found:
            if ACK has been dropped:
                resend ACK
            else:
                decline connection until device is deleted

        :param device: the newly connecting device
        :type device: dict
        :return: a tuple containing the GLOBAL_ID assigned to the device and an optional error message.
        :rtype: tuple
        :raise: KeyError, if the device object lacks necessary information
        """
        return 0, 'not implemented'

    @abc.abstractmethod
    def reconnect_device(self, device):
        """
        Reconnects the given device.

        Check if the device is known by the system and is deemed equal after checking hardware address and LOCAL_ID.
        If the device is accepted, return the GLOBAL_ID assigned to it and a dummy message "success".
        If the device is not accepted, return an invalid GOBAL_ID and an error message specifying the reason.

        The device is specified by a dictionary containing the GLOBAL_ID, LOCAL_ID and HW_ADDR of the device:
            {
                const.GLOBAL_ID: global_id,
                const.LOCAL_ID: local_id,
                const.DEV_HW_ADDRESS: hw_addr
            }

        :param device: the device to be reconnected
        :type device: dict
        :return: a tuple containing the GLOBAL_ID assigned to the device and an optional error message.
        :rtype: tuple
        :raise KeyError, if device object lacks necessary information
        """
        return 0, 'not implemented'

    @abc.abstractmethod
    def save_conf(self, dev, conf):
        """
        Save the adapter configuration for the given device.

        The device is specified by a dictionary containing the GLOBAL_ID of the device:
            {const.GLOBAL_ID: global_id}

        The adapter configuration is defined by a dictionary containing key-value pairs.

        :param dev: the device for which the adapter config is valid
        :type dev: dict
        :param conf: the adapter config
        :type conf: dict
        :raise KeyError, if device object lacks necessary information
        """
        pass

    @abc.abstractmethod
    def device_alive(self, device):
        """
        Inform the monitoring service that the device is alive.

        The device is specified by a dictionary containing the GLOBAL_ID of the device:
            {const.GLOBAL_ID: global_id}

        :param device: the alive device
        :type device: dict
        :raise KeyError, if device object lacks necessary information
        """
        pass

    @abc.abstractmethod
    def get_rmp_id(self, global_id):
        """
        Return the id the rmp application associated with this global_id.
        :param global_id: the global_id to translate
        :type global_id: int
        :return: the rmp application id for the specified global_id
        :rtype: str
        """
        pass


class DiscoveryGateway(abc.ABC):
    def __init__(self, comm_type, service):
        """
        Initialize the gateway with the given communication type and the given service adapter

        :param comm_type: The type of network, the gateway communicates with
        :type comm_type: str
        :param service: The service adapter, the gateway uses to connect to the discovery service
        :type service: ServiceAdapter
        """
        self.comm_type = comm_type
        self.service = service

    @abc.abstractmethod
    def serve_forever(self):
        """
        Start the gateway and handle incoming requests.
        """
        pass

    @abc.abstractmethod
    def shutdown(self):
        """
        Stop the gateway.
        """
        pass


class RMPHandler(abc.ABC):
    """
    This handler class, handles incoming messages from devices.
    For each connection, or for each message if the communication is connection less, a separate handler is used.
    """

    def __init__(self, gateway):
        """

        :param gateway: The gateway which uses the handler
        :type gateway: DiscoveryGateway
        """
        self.gateway = gateway

    @abc.abstractmethod
    def _send_msg(self, msg):
        """
        Send a message to the connected device
        :param msg: message to send
        :type msg: dict
        """
        pass

    @abc.abstractmethod
    def _receive_msg(self):
        """
        Receive a message from the connected device
        :return: the received message
        :rtype: dict
        """
        pass

    @abc.abstractmethod
    def handle(self):
        """
        Handle an incoming connection.
        """
        pass

    def _handle_hello(self, data):
        """
        Handle an incoming HELLO message.
        The data must contain the device's LOCAL_ID, TYPE, IP, HW_ADDR, HOST and optional the GLOBAL_ID:
        {
            const.LOCAL_ID: local_id,
            const.DEV_TYPE: dev_type,
            const.DEV_HW_ADDRESS: hw_addr,
            const.DEV_IP: ip, (optional)
            const.HOST: host, (optional)
            const.GLOBAL_ID: global_id, (optional)
        }

        :param data: information contained in the message
        :type data: dict
        """
        if const.GLOBAL_ID in data:
            global_id, error_message = self.gateway.service.reconnect_device(data)
        else:
            global_id, error_message = self.gateway.service.connect_new_device(data)

        if global_id:  # request was accepted
            reply = {
                const.GLOBAL_ID: global_id,
                const.CONN_TYPE: const.CONN_PING,
            }

        else:  # request was declined
            reply = {
                const.GLOBAL_ID: 0,
                const.ERROR: error_message,
                const.CONN_TYPE: const.CONN_PING,
            }

        self._send_msg(reply)

    def _handle_conf(self, data):
        """
        Handle an incoming CONF message.
        The message must contain the GLOBAL_ID and all adapter parameters.
        {
            const.GLOBAL_ID: global_id,
            <param>:<value>
            .
            .
            .
        }
        :param data: the information contained in the message
        """
        global_id = data[const.GLOBAL_ID]
        device = {const.GLOBAL_ID: global_id}

        # copy all adapter parameters into a new dictionary
        conf = dict()
        for key in data:
            if key not in [const.GLOBAL_ID, const.CONN_TYPE]:
                conf[key] = data[key]

        self.gateway.service.save_conf(device, conf)
        rmp_id = self.gateway.service.get_rmp_id(global_id)
        self._send_msg({
            const.GLOBAL_ID: global_id,
            const.CONNDE_ID: rmp_id,  # hack to deploy android device
            const.CONN_TYPE: const.CONN_PING,
        })  # ACK

    def _handle_ping(self, data):
        """
        Handle an incoming ping message.
        The message may contain arbitrary data.

        If it contains a PING_MSG, it is assmued to be a discovery request.

        If it contains a TIMEOUT value, it is assume to be a heartbeat.

        :param data: information contained in the message
        """
        if const.PING_MSG in data and data[const.PING_MSG] == 'ping':
            data[const.PING_MSG] = 'pong'
            self._send_msg(data)

        if const.GLOBAL_ID in data and const.TIMEOUT in data:
            self.gateway.service.device_alive(data)

    def _handle_value(self, data):
        """
        Handle incoming value.
        Yet to be implemented.
        :param data:
        """
        pass

    def _handle_msg(self, msg):
        """
        Handle a incoming message.
        Delegate the message according to its type.
        :param msg: the incoming message
        :raise NotImplementedException, if the message type is unknown
        """
        connection_types = {
            const.CONN_HELLO: self._handle_hello,
            const.CONN_CONF: self._handle_conf,
            const.CONN_VALUE: self._handle_value,
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
