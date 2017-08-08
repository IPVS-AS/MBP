import abc
import const
import logging as log
import datetime


class ConndeServer(abc.ABC):
    def __init__(self, comm_type, db_client, service):
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


class ConndeHandler(abc.ABC):  # TODO server handler - hide db logic, only access server api not db directly
    def __init__(self, server):
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

    def _register_device_for_monitoring(self, device):
        global_id = device[const.GLOBAL_ID]
        db_key = {const.GLOBAL_ID: global_id}
        timeout = 0
        if const.TIMEOUT in device:
            log.debug('Registering device |%d| for monitoring', global_id)
            timeout = device[const.TIMEOUT]
        elif const.ADAPTER_CONF in device and const.TIMEOUT in device[const.ADAPTER_CONF]:
            timeout = device[const.ADAPTER_CONF][const.TIMEOUT]

        if timeout != 0:
            monitoring_entry = {
                '$set': {
                    const.TIMEOUT: timeout
                },
                '$currentDate': {
                    const.LAST_CONTACT: True
                }
            }
            device_entry = {
                '$set': {
                    const.MONITORING: True
                },
                '$currentDate': {
                    const.LAST_CONTACT: True
                }
            }
            self.server.mon_coll.update_one(db_key, monitoring_entry, upsert=True)
            self.server.dev_coll.update_one(db_key, device_entry)

    def _handle_hello(self, data):
        local_id = data[const.LOCAL_ID]
        dev_ip = data[const.DEV_IP]
        dev_hw_addr = data[const.DEV_HW_ADDRESS]
        dev_type = data[const.DEV_TYPE]
        host = data[const.HOST]

        accepted = True
        error_message = ''

        if const.GLOBAL_ID in data:
            global_id = data[const.GLOBAL_ID]

            db_key = {const.GLOBAL_ID: global_id}
            dev = self.server.dev_coll.find_one(db_key)

            log.debug('Device |%d| trying to reconnect', global_id)
            if dev is None:
                accepted = False
                log.debug('Device |%d| tried to reconnect, but GLOBAL_ID is not known', global_id)
            if dev[const.DEV_HW_ADDRESS] == dev_hw_addr:
                accepted = False
                log.debug('Device |%d| tried to reconnect, but has a different hw_address |%s|', global_id, dev_hw_addr)
            if dev[const.LOCAL_ID] != local_id:
                accepted = False
                log.debug('Device |%d| tried to reconnect, but has a different LOCAL_ID |%s|', global_id, local_id)

            if accepted:
                self._register_device_for_monitoring(dev)
                log.info('Device |%d| reconnected successfully', global_id)
            else:
                error_message = 'Declined'
                log.warning('Device |%d| tried to reconnect, but was declined', global_id)
        else:
            log.debug('Possibly new Device detected |%s@%s|', local_id, dev_hw_addr)
            duplicate_check_key = {
                const.LOCAL_ID: local_id,
                const.DEV_HW_ADDRESS: dev_hw_addr
            }
            duplicate = self.server.dev_coll.find_one(duplicate_check_key)

            if duplicate is not None:
                global_id = duplicate[const.GLOBAL_ID]
                log.debug('Found duplicate for device |%s@%s| with global_id |%d|', local_id, dev_hw_addr, global_id)

                cur_time = datetime.datetime.utcnow()
                if cur_time - duplicate[const.LAST_CONTACT] < datetime.timedelta(seconds=5 * const.CLIENT_TIMEOUT):
                    log.debug('Assuming dropped ACK for device |%d|. Resending...', global_id)
                else:
                    # TODO server - how to deal with duplicates? Strict -> Decline connection, Reassign -> reassign new global_id
                    accepted = False
                    error_message = 'Declined'
                    log.warning('Illegal duplicate |%s@%s| in conflict with device |%d:%s@%s|', local_id, dev_hw_addr,
                                global_id, duplicate[const.LOCAL_ID], duplicate[const.DEV_HW_ADDRESS])
            else:
                global_id = self.server.service.next_global_id()
                log.info('New Device |%s@%s| discovered', local_id, dev_hw_addr)

        if accepted:
            reply = {
                const.DEV_IP: dev_ip,
                const.LOCAL_ID: local_id,
                const.DEV_HW_ADDRESS: dev_hw_addr,
                const.GLOBAL_ID: global_id
            }

            db_key = {
                const.GLOBAL_ID: global_id
            }

            db_entry = {
                '$set': {
                    const.DEV_IP: dev_ip,
                    const.DEV_HW_ADDRESS: dev_hw_addr,
                    const.LOCAL_ID: local_id,
                    const.TYPE: dev_type,
                    const.HOST: host
                },
                '$currentDate': {
                    const.LAST_CONTACT: True
                }
            }

            log.debug('Saving: ' + str(db_entry) + ' for key: ' + str(db_key))

            self.server.dev_coll.update_one(
                db_key,
                db_entry,
                upsert=True
            )
        else:
            reply = {
                const.GLOBAL_ID: 0,
                const.DEV_IP: dev_ip,
                const.LOCAL_ID: local_id,
                const.DEV_HW_ADDRESS: dev_hw_addr,
                const.ERROR: error_message
            }

        self._send_msg(reply)

    def _handle_init(self, data):
        global_id = data[const.GLOBAL_ID]
        db_key = {const.GLOBAL_ID: global_id}

        cur = self.server.dev_coll.find_one(db_key)

        log.debug('Current saved client state: ' + str(cur))

        update = {
            '$set': {
                const.ADAPTER_CONF: {}
            },
            '$currentDate': {
                const.LAST_CONTACT: True
            }
        }

        for key in data:
            if key not in [const.GLOBAL_ID, const.CONN_TYPE]:
                update['$set'][const.ADAPTER_CONF][key] = data[key]

        log.debug('Update client state: ' + str(update))

        self.server.dev_coll.update_one(db_key, update)

        self._register_device_for_monitoring(data)

        self._send_msg({const.GLOBAL_ID: global_id})  # ACK

    def _handle_ping(self, data):
        if const.PING_MSG in data and data[const.PING_MSG] == 'ping':
            data[const.PING_MSG] = 'pong'

        self._send_msg(data)

    def _handle_value(self, data):
        pass

    def _handle_keepalive(self, data):
        global_id = data[const.GLOBAL_ID]
        log.debug('device |%d| is alive', global_id)
        db_key = {const.GLOBAL_ID: global_id}

        db_update = {
            '$currentDate': {
                const.LAST_CONTACT: True
            }
        }

        self.server.dev_coll.update_one(db_key, db_update)
        self.server.mon_coll.update_one(db_key, db_update)

    def _handle_msg(self, msg):
        connection_types = {
            const.CONN_HELLO: self._handle_hello,
            const.CONN_INIT: self._handle_init,
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
