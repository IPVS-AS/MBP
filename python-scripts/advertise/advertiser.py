import abc
import logging as log
import time

from . import advertiseconst as const


class Advertiser(abc.ABC):
    def __init__(self, service, comm_type):
        self.server_address = None
        self.ip = None
        self.hw_addr = None
        self.service = service
        self.comm_type = comm_type

    @abc.abstractmethod
    def _send_msg(self, msg):
        """
        Send a message on the local network.
        :param msg: the message to be send
        :type msg: dict
        """
        pass

    @abc.abstractmethod
    def _receive_msg(self):
        """
        Receive a message from the local network.
        :return: the received message
        :rtype: dict
        """
        pass

    @abc.abstractmethod
    def discover_service(self):
        """
        Discover the RMP's discovery service on the local network
        :return: a tuple containing the service's address, the local ip and the local hardware address
        """
        pass

    def send_keep_alive(self, device_name, timeout):
        """
        Send a heart beat to the discovery service.
        :param device_name: LOCAL_ID of the device
        :type device_name: str
        :param timeout: the next timeout interval
        :type timeout float
        """
        log.debug('Sending keep_alive for |%s|', device_name)
        keep_alive = {
            const.CONN_TYPE: const.CONN_PING,
            const.GLOBAL_ID: self.service.global_ids[device_name],
            const.TIMEOUT: timeout,
        }
        self._send_msg(keep_alive)

    def connect_device(self, device, ip, hw_addr, global_id):
        """
        Connect or reconnect a device.

        Send a HELLO message as registration request.
        Upon successful registration, send a CONF message containing all parameters for the adapter.

        :param device: the device to be connected
        :type device: dict
        :param ip: the local ip
        :param hw_addr: the local hardware address
        :param global_id: the device's GLOBAL_ID if available
        :type global_id: int
        :return the received GLOBAL_ID or 0 if no connection possible
        :rtype: int
        :raise KeyError, if the device object does not contain all necessary information
        """

        # send hello message
        hello_msg = {
            const.DEV_IP: ip,
            const.DEV_HW_ADDRESS: hw_addr.lower(),
            const.DEV_TYPE: device[const.DEV_TYPE],
            const.LOCAL_ID: device[const.LOCAL_ID],
            const.CONN_TYPE: const.CONN_HELLO
        }

        if const.HOST in device:
            hello_msg[const.HOST] = device[const.HOST]

        if global_id:
            hello_msg[const.GLOBAL_ID] = global_id

        self._send_msg(hello_msg)

        global_id = 0  # will be overwritten upon successful connection
        hello_reply = self._receive_msg()
        if hello_reply:
            if const.GLOBAL_ID in hello_reply and hello_reply[const.GLOBAL_ID]:  # valid GLOBAL_ID == success
                global_id = hello_reply[const.GLOBAL_ID]
                # create CONF message with GLOBAL_ID and all parameters from ADAPTER_CONF
                conf_msg = {
                    const.GLOBAL_ID: global_id,
                    const.CONN_TYPE: const.CONN_CONF
                }

                adapter_conf = device[const.ADAPTER_CONF]
                for key in adapter_conf:
                    conf_msg[key] = adapter_conf[key]

                self._send_msg(conf_msg)

                log.debug('Waiting for ACK')
                ack = self._receive_msg()
                if not ack or const.GLOBAL_ID not in ack or ack[const.GLOBAL_ID] != global_id:
                    log.debug('Did not recieve valid ACK. Treating device as unconnected')
                    global_id = 0  # treat as unconnected

            elif const.ERROR in hello_reply:
                log.info('Could not connect device |%s|. Reason |%s|', device[const.LOCAL_ID], hello_reply[const.ERROR])
        return global_id

    def advertise(self):
        """
        Discover the discovery service and connect every device to it.
        """
        # discover server
        tries = 0
        while self.server_address is None and tries < 5:  # maximum 5 discovery tries
            tries += 1
            log.debug('discovering server; try |%d|', tries)
            server = self.discover_service()
            if server:
                self.server_address = server[0]
                self.ip = server[1]
                self.hw_addr = server[2]
            time.sleep(const.CLIENT_SLEEPTIME)

        log.info('Server found @ |%s| after |%d| tries', str(self.server_address), tries)

        if self.server_address is not None:
            # connect all devices beginning with the host
            host = self.service.host
            if host is not None:
                global_id = self.service.global_ids[host[const.LOCAL_ID]]
                if global_id:
                    log.info('Reconnecting device |%s|', host[const.LOCAL_ID])
                else:
                    log.info('Connecting device |%s|', host[const.LOCAL_ID])

                global_id = self.connect_device(host, self.ip, self.hw_addr, global_id)
                if global_id:
                    log.info('Connected device |%s| with GLOBAL_ID |%d|', host[const.LOCAL_ID], global_id)
                    self.service.connected[host[const.LOCAL_ID]] = True
                    self.service.global_ids[host[const.LOCAL_ID]] = global_id
                else:
                    log.error('Could not connect host. Aborting advertising...')
                    return

            for device in self.service.autodeploy_data[const.DEPLOY_DEVICES]:
                if host is not None:
                    # add host to the device
                    device[const.HOST] = self.service.global_ids[host[const.LOCAL_ID]]
                else:
                    device[const.HOST] = ''
                global_id = self.service.global_ids[device[const.LOCAL_ID]]
                if global_id:
                    log.info('Reconnecting device |%s|', device[const.LOCAL_ID])
                else:
                    log.info('Connecting device |%s|', device[const.LOCAL_ID])
                global_id = self.connect_device(device, self.ip, self.hw_addr, global_id)
                if global_id:
                    log.info('Connected device |%s| with GLOBAL_ID |%d|', device[const.LOCAL_ID], global_id)
                    self.service.connected[device[const.LOCAL_ID]] = True
                    self.service.global_ids[device[const.LOCAL_ID]] = global_id
