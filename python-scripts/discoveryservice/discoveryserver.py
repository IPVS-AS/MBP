import abc
import const
import logging as log


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

    def _handle_hello(self, data):
        log.debug('Handling hello message')

        local_id = data[const.LOCAL_ID]
        dev_ip = data[const.DEV_IP]
        dev_hw_addr = data[const.DEV_HW_ADDRESS]
        dev_type = data[const.DEV_TYPE]
        host = data[const.HOST]

        if const.GLOBAL_ID in data:
            log.info('Device reentering environment')
            global_id = data[const.GLOBAL_ID]
            # TODO server - register reconnected device for monitoring
        else:
            log.info('New Device discovered')
            global_id = self.server.service.next_global_id()

        # TODO server - check if new device is accepted

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

        self._send_msg(reply)

    def _handle_init(self, data):
        log.debug('Handling adapter config')
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

        if const.TIMEOUT in data:
            log.debug('Registering device |%d| for monitoring', global_id)
            timeout = data[const.TIMEOUT]
            monitoring_entry = {
                '$set': {
                    const.TIMEOUT: timeout
                },
                '$currentDate': {
                    const.LAST_CONTACT: True
                }
            }
            self.server.mon_coll.update_one(db_key, monitoring_entry, upsert=True)

            # auto_data = self.server.dev_coll.find_one(db_key)
            # del auto_data['_id']
            # autodeploy(auto_data)

    def _handle_ping(self, data):
        log.debug('handle ping request')
        if const.PING_MSG in data and data[const.PING_MSG] == 'ping':
            data[const.PING_MSG] = 'pong'

        self._send_msg(data)

    def _handle_value(self, data):
        pass

    def _handle_keepalive(self, data):
        log.debug('handle keep alive')
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
