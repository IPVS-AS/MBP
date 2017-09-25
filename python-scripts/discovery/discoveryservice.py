from discovery.discoverygatewaylan import *
from discovery.discoverygatewaybt import *

import datetime
import threading
import requests

from pymongo import MongoClient


class DiscoveryService(ServiceAdapter):
    def __init__(self):
        self.db_client = MongoClient()
        db = self.db_client[const.DB_NAME]
        self.dev_coll = db[const.DEV_COLL_NAME]
        self.mon_coll = db[const.MONITOR_COLL_NAME]

        self.status_coll = db[const.STATUS_COLL_NAME]
        init_status = self.status_coll.find_one({const.STATUS_FOR: const.SERVER_SERVICE})
        if const.STATUS_NEXT_ID in init_status:
            self._next_id = int(init_status[const.STATUS_NEXT_ID])
            log.info('restored init status with next_id=|%d|', self._next_id)
        else:
            log.warning('no valid init status found')
            self._next_id = 100

        # Connde database
        connde_db = self.db_client[const.CONNDE_DB_NAME]
        self.connde_devices = connde_db[const.CONNDE_DEVICE_COLLECTION]
        self.connde_sensors = connde_db[const.CONNDE_SENSOR_COLLECTION]
        self.connde_types = connde_db[const.CONNDE_TYPE_COLLECTION]

        self.id_lock = threading.Lock()

        self.servers = []
        self.serverthreads = dict()

    def next_global_id(self):
        self.id_lock.acquire()
        tmp = self._next_id
        self._next_id += 1
        self.id_lock.release()
        return tmp

    def start(self, lan=True, bt=True):
        log.info('Starting discovery service...')

        if lan:
            log.info('Starting LAN...')
            lan_server = ConndeLanGateway(ConndeLanHandler, self.db_client, self, ('', const.PORT))
            lan_thread = threading.Thread(target=lan_server.serve_forever)
            lan_thread.start()
            self.servers.append(lan_server)
            self.serverthreads[lan_server.comm_type] = lan_thread

        if bt:
            log.info('Starting BT...')
            bt_server = ConndeBluetoothGateway(ConndeBluetoothHandler, self.db_client, self)
            bt_thread = threading.Thread(target=bt_server.serve_forever)
            bt_thread.start()
            self.servers.append(bt_server)
            self.serverthreads[bt_server.comm_type] = bt_thread

        log.info('Waiting for messages...')

    def stop(self):
        log.info('Stopping discovery service')

        for server in self.servers:
            log.info('Shutting down %s...', server.comm_type)
            server.shutdown()
            self.serverthreads[server.comm_type].join()

        log.info('saving service status...')
        self.status_coll.update_one({const.STATUS_FOR: const.SERVER_SERVICE},
                                    {
                                        '$set': {const.STATUS_NEXT_ID: self._next_id}
                                    })
        self.db_client.close()

    def register_device_for_monitoring(self, device):
        """
        Register the given device with the monitoring service.

        :param device: the device to be monitored
        :type device: dict

        :return: nothing
        """
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
            self.mon_coll.update_one(db_key, monitoring_entry, upsert=True)
            self.dev_coll.update_one(db_key, device_entry)

    def _insert_to_connde_db(self, device):
        """
        Insert the given device into the connde database.
        Upon the const.HOST value of the device it is determined if it is a sensor or full device.
        The devices is specified by a dictionary containing all necessary keys to fill the connde database:
        {
            const.LOCAL_ID:,
            const.DEV_HW_ADDRESS:,
            const.DEV_IP:,
            const.HOST:,
            const.DEV_TYPE:,
            const.GLOBAL_ID:,
        }
        :param device: the device to be inserted to the connde database
        :type device: dict
        :return:
        """
        local_id = device[const.LOCAL_ID]
        dev_hw_addr = device[const.DEV_HW_ADDRESS]
        dev_ip = device[const.DEV_IP]
        if const.HOST in device:
            host = device[const.HOST]
        else:
            host = ''
        dev_type = device[const.DEV_TYPE]
        global_id = device[const.GLOBAL_ID]

        if host:
            host_key = {
                const.GLOBAL_ID: host
            }

            db_host = self.connde_devices.find_one(host_key)

            type_key = {
                const.CONNDE_TYPE_NAME: dev_type
            }

            db_type = self.connde_types.find_one(type_key)

            connde_sensor = {
                '$set': {
                    const.CONNDE_SENSOR_CLASS: const.CONNDE_SENSOR_JAVA_CLASS,
                    const.CONNDE_SENSOR_NAME: local_id,
                    const.CONNDE_SENSOR_TYPE: db_type,
                    const.CONNDE_SENSOR_DEVICE: db_host,
                    const.GLOBAL_ID: global_id,
                }
            }

            sensor_key = {
                const.GLOBAL_ID: global_id,
            }

            self.connde_sensors.update_one(sensor_key, connde_sensor, upsert=True)

        else:  # if there is no host, we assume a device
            connde_device = {
                '$set': {
                    const.CONNDE_DEVICE_AUTODEPLOY: False,
                    const.CONNDE_DEVICE_IP: dev_ip,
                    const.CONNDE_DEVICE_MAC: str(dev_hw_addr).replace(':', ''),
                    const.CONNDE_DEVICE_IFAC: 'iface',
                    const.CONNDE_DEVICE_NAME: local_id,
                    const.GLOBAL_ID: global_id,
                },
                '$currentDate': {
                    const.CONNDE_DEVICE_DATE: True
                }
            }

            device_key = {
                const.GLOBAL_ID: global_id,
            }

            self.connde_devices.update_one(device_key, connde_device, upsert=True)

    def connect_new_device(self, device):
        """
        Connects a new device with the Connde system.

        Check for a duplicate in the system.
        In case a duplicate was found:
            if ACK has been dropped:
                resend ACK
            else:
                decline connection until device is deleted

        :param device: the newly connecting device
        :type device: dict
        :return: a tuple containing the GLOBAL_ID assigned to the device and an optional error message.
        """
        local_id = device[const.LOCAL_ID]
        dev_hw_addr = device[const.DEV_HW_ADDRESS]
        dev_ip = device[const.DEV_IP]
        if const.HOST in device:
            host = device[const.HOST]
        else:
            host = ''
        dev_type = device[const.DEV_TYPE]

        log.debug('Possibly new Device detected |%s@%s|', local_id, dev_hw_addr)
        accepted = True
        error_message = 'success'
        duplicate_check_key = {
            const.LOCAL_ID: local_id,
            const.DEV_HW_ADDRESS: dev_hw_addr
        }
        duplicate = self.dev_coll.find_one(duplicate_check_key)

        if duplicate is not None:
            global_id = duplicate[const.GLOBAL_ID]
            log.debug('Found duplicate for device |%s@%s| with global_id |%d|', local_id, dev_hw_addr, global_id)

            cur_time = datetime.datetime.utcnow()
            if cur_time - duplicate[const.LAST_CONTACT] < datetime.timedelta(
                    seconds=5 * const.CLIENT_TIMEOUT):  # TODO service - adjust time delta
                log.debug('Assuming dropped ACK for device |%d|. Resending...', global_id)
            else:
                # TODO server - how to deal with duplicates? Strict -> Decline connection, Reassign -> reassign new global_id
                accepted = False
                error_message = 'Declined'
                log.warning('Illegal duplicate |%s@%s| in conflict with device |%d:%s@%s|', local_id, dev_hw_addr,
                            global_id, duplicate[const.LOCAL_ID], duplicate[const.DEV_HW_ADDRESS])
        else:
            global_id = self.next_global_id()
            log.info('New Device |%s@%s| discovered', local_id, dev_hw_addr)

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

            self.dev_coll.update_one(
                db_key,
                db_entry,
                upsert=True
            )

            # insert to connde database
            device[const.GLOBAL_ID] = global_id
            self._insert_to_connde_db(device)

        if not accepted:
            global_id = 0

        return global_id, error_message

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
        """
        global_id = device[const.GLOBAL_ID]
        dev_hw_addr = device[const.DEV_HW_ADDRESS]
        local_id = device[const.LOCAL_ID]

        db_key = {const.GLOBAL_ID: global_id}
        dev = self.dev_coll.find_one(db_key)

        log.debug('Device |%d| trying to reconnect', global_id)
        accepted = True
        error_message = 'success'
        if dev is None:
            accepted = False
            error_message = 'GLOBAL_ID is not known'
            log.debug('Device |%d| tried to reconnect, but GLOBAL_ID is not known', global_id)
        elif dev[const.DEV_HW_ADDRESS] != dev_hw_addr:
            accepted = False
            error_message = 'different hw_address'
            log.debug('Device |%d| tried to reconnect, but has a different hw_address |%s| != |%s|', global_id,
                      dev_hw_addr, dev[const.DEV_HW_ADDRESS])
        elif dev[const.LOCAL_ID] != local_id:
            accepted = False
            error_message = 'different LOCAL_ID'
            log.debug('Device |%d| tried to reconnect, but has a different LOCAL_ID |%s|', global_id, local_id)

        if accepted:
            self.register_device_for_monitoring(dev)
            self._insert_to_connde_db(dev)
            log.info('Device |%d| reconnected successfully', global_id)
        else:
            log.warning('Device |%d| tried to reconnect, but was declined', global_id)
            global_id = 0

        return global_id, error_message

    def save_init(self, dev, init):
        """
        Save the adapter configuration for the given device.

        The device is specified by a dictionary containing the GLOBAL_ID of the device:
            {const.GLOBAL_ID: global_id}

        The adapter configuration is defined by a dictionary containing key-value pairs.

        :param dev: the device for which the adapter config is valid
        :type dev: dict
        :param init: the adapter config
        :type init: dict
        :return: nothing
        """
        global_id = dev[const.GLOBAL_ID]
        db_key = {const.GLOBAL_ID: global_id}

        cur = self.dev_coll.find_one(db_key)

        log.debug('Current saved client state: ' + str(cur))

        update = {
            '$set': {
                const.ADAPTER_CONF: {}
            },
            '$currentDate': {
                const.LAST_CONTACT: True
            }
        }

        for key in init:
            if key not in [const.GLOBAL_ID, const.CONN_TYPE]:
                update['$set'][const.ADAPTER_CONF][key] = init[key]

        log.debug('Update client state: ' + str(update))

        self.dev_coll.update_one(db_key, update)

        if const.TIMEOUT in init:
            timeout = init[const.TIMEOUT]
            monitor_device = {
                const.GLOBAL_ID: global_id,
                const.TIMEOUT: timeout,
            }
            self.register_device_for_monitoring(monitor_device)

        connde_sensor = self.connde_sensors.find_one(db_key)
        if connde_sensor is not None:
            sensor_id = connde_sensor['_id']

            if sensor_id is not None and 'pinset' in init:
                requests.post("http://localhost:8080/MBP/deploy/sensor/" + str(sensor_id),
                              data={'component': 'SENSOR', 'pinset': init[const.PINSET]})

    def device_alive(self, device):
        """
        Inform the monitoring service that the device is alive.

        The device is specified by a dictionary containing the GLOBAL_ID of the device:
            {const.GLOBAL_ID: global_id}

        :param device: the alive device
        :type device: dict
        :return: nothing
        """

        global_id = device[const.GLOBAL_ID]
        log.debug('device |%d| is alive', global_id)
        db_key = {const.GLOBAL_ID: global_id}

        db_update = {
            '$currentDate': {
                const.LAST_CONTACT: True
            }
        }

        self.dev_coll.update_one(db_key, db_update)
        self.mon_coll.update_one(db_key, db_update)
