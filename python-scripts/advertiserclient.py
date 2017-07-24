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
        global_id = 0

        # send hello message
        hello_msg = {
            const.DEV_IP: ip,
            const.DEV_HW_ADDRESS: hw_addr.lower(),
            const.DEV_TYPE: device[const.DEV_TYPE],
            const.LOCAL_ID: device[const.NAME],
            const.CONN_TYPE: const.CONN_HELLO
        }
        self._send_msg(hello_msg)

        hello_reply = self._receive_msg()
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

        return global_id

    def advertise(self):
        while self.server_address is None:
            print('advertising client')
            self.discover_server()
            time.sleep(const.SLEEPTIME)

        print('Server found @ |' + str(self.server_address) + '|')

        if self.server_address is not None:
            for sensor in self.service.autodeploy_data[const.DEPLOY_SENSORS]:  # set invalid id for all devices
                global_id = self.connect_device(sensor, self.ip, self.hw_addr)
                if global_id:
                    self.service.global_ids[sensor[const.LOCAL_ID]] = global_id
