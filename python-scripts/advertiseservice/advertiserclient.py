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

    def send_keep_alive(self, device_name):
        log.debug('Sending keep_alive for |%s|', device_name)
        keep_alive = {
            const.CONN_TYPE: const.CONN_KEEP_ALIVE,
            const.GLOBAL_ID: self.service.global_ids[device_name]
        }
        self._send_msg(keep_alive)

    def connect_device(self, device, ip, hw_addr, global_id):
        # TODO client - do not send adapter config upon reconnect

        if const.HOST in device:
            host = device[const.HOST]
        else:
            host = ''

        # send hello message
        hello_msg = {
            const.DEV_IP: ip,
            const.DEV_HW_ADDRESS: hw_addr.lower(),
            const.DEV_TYPE: device[const.DEV_TYPE],
            const.LOCAL_ID: device[const.LOCAL_ID],
            const.HOST: host,
            const.CONN_TYPE: const.CONN_HELLO
        }

        self._send_msg(hello_msg)

        hello_reply = self._receive_msg()
        if hello_reply:
            if const.GLOBAL_ID in hello_reply:  # check for valid server response
                global_id = hello_reply[const.GLOBAL_ID]
            if const.ERROR in hello_reply:
                log.info('Could not connect device |%s|. Reason |%s|', device[const.LOCAL_ID], hello_reply[const.ERROR])

        if global_id:
            # send init message
            init_msg = {
                const.GLOBAL_ID: global_id,
                const.CONN_TYPE: const.CONN_INIT
            }

            adapter_conf = device[const.ADAPTER_CONF]
            for key in adapter_conf:
                init_msg[key] = adapter_conf[key]

            self._send_msg(init_msg)
            log.debug('Waiting for ACK')
            ack = self._receive_msg()
            if not ack or const.GLOBAL_ID not in ack or ack[const.GLOBAL_ID] != global_id:
                log.debug('Did not recieve valid ACK. Treating device as unconnected')
                global_id = 0  # treat as unconnected

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
