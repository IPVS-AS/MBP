import time
import const
import logging as log
import abc


class AdvertiserClient(abc.ABC):
    def __init__(self, service, comm_type):
        self.server_address = None
        self.ip = None
        self.hw_addr = None
        self.service = service
        self.comm_type = comm_type

    @abc.abstractmethod
    def _send_msg(self, msg):
        pass

    @abc.abstractmethod
    def _receive_msg(self):
        pass

    @abc.abstractmethod
    def discover_server(self):
        pass

    def connect_device(self, device, ip, hw_addr):
        log.info('Connecting device |' + device[const.NAME] + '|')
        # TODO advertiseclient - read global id from last connection if availble; seperate method reconnect?
        global_id = 0  # zero will be treated as false

        if const.HOST in device:
            host = device[const.HOST]
        else:
            host = ''

        # send hello message
        hello_msg = {
            const.DEV_IP: ip,
            const.DEV_HW_ADDRESS: hw_addr.lower(),
            const.DEV_TYPE: device[const.DEV_TYPE],
            const.LOCAL_ID: device[const.NAME],
            const.HOST: host,
            const.CONN_TYPE: const.CONN_HELLO
        }

        self._send_msg(hello_msg)

        hello_reply = self._receive_msg()
        if hello_reply and const.GLOBAL_ID in hello_reply:  # check for valid server response
            global_id = hello_reply[const.GLOBAL_ID]

            # send init message
            init_msg = {
                const.GLOBAL_ID: global_id,
                const.CONN_TYPE: const.CONN_INIT
            }

            adapter_conf = device[const.ADAPTER_CONF]
            for key in adapter_conf:
                init_msg[key] = adapter_conf[key]

            self._send_msg(init_msg)
            # TODO advertiseclient - wait for ack

        return global_id

    def advertise(self):
        tries = 0
        while self.server_address is None:
            tries += 1
            log.debug('discovering server; try |%d|', tries)
            server = self.discover_server()
            if server:
                srv_addr = server[0]
                own_ip = server[1]
                own_hw_addr = server[2]
                self.server_address = srv_addr
                self.ip = own_ip
                self.hw_addr = own_hw_addr
            time.sleep(const.SLEEPTIME)

        log.info('Server found @ |%s| after |%d| tries', str(self.server_address), tries)

        if self.server_address is not None:
            host = self.service.host
            if host is not None:
                global_id = self.connect_device(host, self.ip, self.hw_addr)
                if global_id:
                    self.service.global_ids[host[const.NAME]] = global_id
                else:
                    log.error('Could not connect host. Aborting advertising...')

            for sensor in self.service.autodeploy_data[const.DEPLOY_DEVICES]:
                if host is not None:
                    # add host to the device
                    sensor[const.HOST] = self.service.global_ids[host[const.NAME]]
                else:
                    sensor[const.HOST] = ''

                global_id = self.connect_device(sensor, self.ip, self.hw_addr)
                if global_id:
                    self.service.global_ids[sensor[const.NAME]] = global_id
