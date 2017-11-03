import datetime
import threading

import requests
from pymongo import MongoClient

from .discoverygatewaybt import *
from .discoverygatewaylan import *
from . import discoveryconst as const


class DiscoveryService(ServiceAdapter):
    def __init__(self):
        """
        Initialize the discovery service and the database connections.
        Note: Gateways are started using the start() method
        """
        self.db_client = MongoClient()

        # initialize db collections
        self.dev_coll = self.db_client[const.DB_NAME][const.DEV_COLL_NAME]
        self.mon_coll = self.db_client[const.DB_NAME][const.MONITOR_COLL_NAME]
        self.status_coll = self.db_client[const.DB_NAME][const.STATUS_COLL_NAME]

        # read status from db
        init_status = self.status_coll.find_one({const.STATUS_FOR: const.SERVER_SERVICE})
        if const.STATUS_NEXT_ID in init_status:
            self._next_id = int(init_status[const.STATUS_NEXT_ID])
            log.info('restored init status with next_id=|%d|', self._next_id)
        else:
            log.warning('no valid init status found')
            self._next_id = 100

        # Connde database
        self.rmp_devices = self.db_client[const.RMP_DB_NAME][const.RMP_DEVICE_COLLECTION]
        self.rmp_sensors = self.db_client[const.RMP_DB_NAME][const.RMP_SENSOR_COLLECTION]
        self.rmp_types = self.db_client[const.RMP_DB_NAME][const.RMP_TYPE_COLLECTION]

        self.id_lock = threading.Lock()

        # lists of gateways
        self.gateways = []
        self.gatewaythreads = dict()

    def _next_global_id(self):
        """
        Atomically generate the next GLOBAL_ID
        :return: New unique GLOBAL_ID
        """
        self.id_lock.acquire()
        tmp = self._next_id
        self._next_id += 1
        self.id_lock.release()
        return tmp

    def start(self, lan=True, bt=True):
        """
        Start the discovery service.
        Initialize the specified gateways and start each in a separate thread.
        :param lan: Specifies if the gateway for IP-based networks should be started. Defaults to true.
        :param bt: Specifies if the gateway for Bluetooth networks should be started. Defaults to true.
        """
        log.info('Starting discovery service...')

        if lan:
            log.info('Starting LAN...')
            lan_server = DiscoveryLanGateway(self)
            lan_thread = threading.Thread(target=lan_server.serve_forever)
            lan_thread.start()
            self.gateways.append(lan_server)
            self.gatewaythreads[lan_server.comm_type] = lan_thread

        if bt:
            log.info('Starting BT...')
            bt_server = DiscoveryBluetoothGateway(self)
            bt_thread = threading.Thread(target=bt_server.serve_forever)
            bt_thread.start()
            self.gateways.append(bt_server)
            self.gatewaythreads[bt_server.comm_type] = bt_thread

        log.info('Waiting for messages...')

    def stop(self):
        """
        Stop all gateways and the discovery service.
        """
        log.info('Stopping discovery service')

        for server in self.gateways:
            log.info('Shutting down %s...', server.comm_type)
            server.shutdown()
            self.gatewaythreads[server.comm_type].join()

        log.info('saving service status...')
        self.status_coll.update_one({const.STATUS_FOR: const.SERVER_SERVICE},
                                    {
                                        '$set': {const.STATUS_NEXT_ID: self._next_id}
                                    })
        self.db_client.close()

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
        # read all necessary information, throws exception if not present
        local_id = device[const.LOCAL_ID]
        dev_hw_addr = device[const.DEV_HW_ADDRESS]
        dev_ip = device[const.DEV_IP]
        if const.HOST in device:
            host = device[const.HOST]
        else:
            host = ''
        dev_type = device[const.DEV_TYPE]

        log.debug('Possibly new Device detected |%s@%s|', local_id, dev_hw_addr)

        # init values
        accepted = True
        error_message = 'success'

        duplicate_check_key = {  # key to search the database for possible duplicates
            const.LOCAL_ID: local_id,
            const.DEV_HW_ADDRESS: dev_hw_addr
        }
        duplicate = self.dev_coll.find_one(duplicate_check_key)

        if duplicate is not None:  # duplicate is found
            global_id = duplicate[const.GLOBAL_ID]
            log.debug('Found duplicate for device |%s@%s| with global_id |%d|', local_id, dev_hw_addr, global_id)

            # check whether the duplicate request may be due to a lost acknowledgment.
            # If so, resend the ack, otherwise decline the request
            cur_time = datetime.datetime.utcnow()
            if cur_time - duplicate[const.LAST_CONTACT] < datetime.timedelta(
                    seconds=5 * const.DISCOVERY_TIMEOUT):
                log.debug('Assuming dropped ACK for device |%d|. Resending...', global_id)
            else:
                accepted = False
                error_message = 'Declined'
                log.warning('Illegal duplicate |%s@%s| in conflict with device |%d:%s@%s|', local_id, dev_hw_addr,
                            global_id, duplicate[const.LOCAL_ID], duplicate[const.DEV_HW_ADDRESS])
        else:  # no duplicate found -> accept request and register device
            global_id = self._next_global_id()
            log.info('New Device |%s@%s| discovered', local_id, dev_hw_addr)

            # create db entry. Use update instead of insert to use '$currentDate'
            db_key = {
                const.GLOBAL_ID: global_id
            }

            db_entry = {
                '$set': {
                    const.DEV_IP: dev_ip,
                    const.DEV_HW_ADDRESS: dev_hw_addr,
                    const.LOCAL_ID: local_id,
                    const.DEV_TYPE: dev_type,
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

        if not accepted: # invalidate global_id if not accepted
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
        :rtype: tuple
        :raise KeyError, if device object lacks necessary information
        """
        # read all necessary information, throws exception if not present
        global_id = device[const.GLOBAL_ID]
        dev_hw_addr = device[const.DEV_HW_ADDRESS]
        local_id = device[const.LOCAL_ID]

        log.debug('Device |%d| trying to reconnect', global_id)

        # search registered devices for given GLOBAL_ID
        db_key = {const.GLOBAL_ID: global_id}
        dev = self.dev_coll.find_one(db_key)

        # init values
        accepted = True
        error_message = 'success'

        # check whether provided information equals the stored information. If not, decline.
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
            self._register_device_for_monitoring(dev)
            self._insert_to_connde_db(dev)
            log.info('Device |%d| reconnected successfully', global_id)
        else:
            log.warning('Device |%d| tried to reconnect, but was declined', global_id)
            global_id = 0

        return global_id, error_message

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
        # read all necessary information, throws exception if not present
        global_id = dev[const.GLOBAL_ID]

        # find device
        db_key = {const.GLOBAL_ID: global_id}
        cur = self.dev_coll.find_one(db_key)

        log.debug('Current saved client state: ' + str(cur))

        # generate dict to update device in database
        update = {
            '$set': {
                const.ADAPTER_CONF: {}
            },
            '$currentDate': {
                const.LAST_CONTACT: True
            }
        }

        # As possible parameters are not known, copy all message contents into the dict
        for key in conf:
            if key not in [const.GLOBAL_ID, const.CONN_TYPE]:
                update['$set'][const.ADAPTER_CONF][key] = conf[key]

        log.debug('Update client state: ' + str(update))
        self.dev_coll.update_one(db_key, update)

        # if necessary register device for monitoring
        if const.TIMEOUT in conf:
            timeout = conf[const.TIMEOUT]
            monitor_device = {
                const.GLOBAL_ID: global_id,
                const.TIMEOUT: timeout,
            }
            self._register_device_for_monitoring(monitor_device)

        # if the device is a sensor, and the conf object contains the 'pinset' parameter, deploy
        rmp_sensor = self.rmp_sensors.find_one(db_key)
        if rmp_sensor is not None:
            sensor_id = rmp_sensor['_id']

            if sensor_id is not None and 'pinset' in conf:
                requests.post("http://localhost:8080/MBP/api/deploy/sensor/" + str(sensor_id),
                              json={'component': 'SENSOR', 'pinset': conf[const.PINSET]})

    def device_alive(self, device):
        """
        Inform the monitoring service that the device is alive.

        The device is specified by a dictionary containing the GLOBAL_ID of the device:
            {const.GLOBAL_ID: global_id}

        :param device: the alive device
        :type device: dict
        :raise KeyError, if device object lacks necessary information
        """
        # read all necessary information, throws exception if not present
        global_id = device[const.GLOBAL_ID]

        log.debug('device |%d| is alive', global_id)
        db_key = {const.GLOBAL_ID: global_id}

        db_update = {
            '$currentDate': {
                const.LAST_CONTACT: True
            }
        }

        # update device and monitoring collection
        self.dev_coll.update_one(db_key, db_update)
        self.mon_coll.update_one(db_key, db_update)

    def get_rmp_id(self, global_id):
        """
        Return the id the rmp application associated with this global_id.
        :param global_id: the global_id to translate
        :type global_id: int
        :return: the rmp application id for the specified global_id
        :rtype: str
        """
        db_key = {const.GLOBAL_ID: global_id}
        device = self.rmp_devices.find_one(db_key)
        if device is None:
            device = self.rmp_sensors.find_one(db_key)
        if device is not None:
            rmp_id = device[const.MONGO_ID]
            return str(rmp_id)
        else:
            return ''

    def _register_device_for_monitoring(self, device):
        """
        Register the given device with the monitoring service.
        The timeout value is interpreted in seconds.

        The device is a dictionary containing the device's GLOBAL_ID and the TIMEOUT parameter.
        The TIMEOUT is either specified on the first level or in a nested dictionary ADAPTER_CONF:

        {
            const.GLOBAL_ID: global_id,
            const.TIMEOUT: timeout
        }

        or

        {
            const.GLOBAL_ID: global_id,
            const.ADAPTER_CONF:  {
                const.TIMEOUT: timeout
            }
        }

        :param device: the device to be monitored
        :type device: dict
        :raise KeyError, if device object lacks necessary information
        """
        # read all necessary information, throws exception if not present
        global_id = device[const.GLOBAL_ID]

        db_key = {const.GLOBAL_ID: global_id}
        timeout = 0
        if const.TIMEOUT in device:
            log.debug('Registering device |%d| for monitoring', global_id)
            timeout = device[const.TIMEOUT]
        elif const.ADAPTER_CONF in device and const.TIMEOUT in device[const.ADAPTER_CONF]:
            timeout = device[const.ADAPTER_CONF][const.TIMEOUT]

        if timeout != 0:
            # insert into monitoring collection
            monitoring_entry = {
                '$set': {
                    const.TIMEOUT: timeout
                },
                '$currentDate': {
                    const.LAST_CONTACT: True
                }
            }
            # update in the device collection
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
        :raise KeyError, if device object lacks necessary information
        """
        # read all necessary information, throws exception if not present
        local_id = device[const.LOCAL_ID]
        dev_hw_addr = device[const.DEV_HW_ADDRESS]
        dev_ip = device[const.DEV_IP]
        if const.HOST in device:
            host = device[const.HOST]
        else:
            host = ''
        dev_type = device[const.DEV_TYPE]
        global_id = device[const.GLOBAL_ID]

        # if a host is specified the device is treated as a sensor or actuator, else it is treated as a device
        if host:
            # load hosting device
            host_key = {
                const.GLOBAL_ID: host
            }
            db_host = self.rmp_devices.find_one(host_key)

            # load type
            type_key = {
                const.RMP_TYPE_NAME: dev_type
            }
            db_type = self.rmp_types.find_one(type_key)

            # TODO discovery - differentiate between actuator and sensor

            # save to database
            rmp_sensor = {
                '$set': {
                    const.RMP_SENSOR_CLASS: const.CONNDE_SENSOR_JAVA_CLASS,
                    const.RMP_SENSOR_NAME: local_id,
                    const.RMP_SENSOR_TYPE: db_type,
                    const.RMP_SENSOR_DEVICE: db_host,
                    const.GLOBAL_ID: global_id,
                }
            }

            sensor_key = {
                const.GLOBAL_ID: global_id,
            }

            self.rmp_sensors.update_one(sensor_key, rmp_sensor, upsert=True)

        else:  # if there is no host, we assume a device
            rmp_device = {
                '$set': {
                    const.RMP_DEVICE_AUTODEPLOY: False,
                    const.RMP_DEVICE_IP: dev_ip,
                    const.RMP_DEVICE_MAC: str(dev_hw_addr).replace(':', ''),
                    const.RMP_DEVICE_IFAC: 'iface',
                    const.RMP_DEVICE_NAME: local_id,
                    const.GLOBAL_ID: global_id,
                },
                '$currentDate': {
                    const.RMP_DEVICE_DATE: True
                }
            }

            device_key = {
                const.GLOBAL_ID: global_id,
            }

            self.rmp_devices.update_one(device_key, rmp_device, upsert=True)
