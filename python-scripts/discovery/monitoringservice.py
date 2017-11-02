import discovery.discoveryconst as const

import datetime
import logging as log
from pymongo import MongoClient
import time
import threading


class MonitoringService:
    def __init__(self):
        self.db_client = MongoClient()
        self.dev_coll = self.db_client[const.DISCOVERY_DB_NAME][const.MONITOR_COLL_NAME]
        self.connde_devices = self.db_client[const.CONNDE_DB_NAME][const.CONNDE_DEVICE_COLLECTION]
        self.connde_sensors = self.db_client[const.CONNDE_DB_NAME][const.CONNDE_SENSOR_COLLECTION]
        self.monitor = False

    def start(self):
        log.info('Start monitoring')
        self.monitor = True
        t = threading.Thread(target=self._loop_forever)
        t.start()

    def _loop_forever(self):
        while self.monitor:
            cur_time = datetime.datetime.utcnow()
            log.debug('Monitoring checking devices @ |%s|', str(cur_time))
            dev_cursor = self.dev_coll.find({})  # load all devices

            to_delete = []  # list of GLOBAL_IDs to delete

            for monitored_device in dev_cursor:
                if const.GLOBAL_ID not in monitored_device:
                    log.warning('no GLOBAL_ID specified for device |%s|', str(monitored_device))
                    continue
                global_id = monitored_device[const.GLOBAL_ID]

                if const.LAST_CONTACT not in monitored_device:
                    log.warning('no last contact noted for device |%s|', str(monitored_device))
                    continue
                last_contact = monitored_device[const.LAST_CONTACT]

                if const.TIMEOUT not in monitored_device:
                    log.warning('no timeout specified for device |%s|', str(monitored_device))
                    continue
                timeout = datetime.timedelta(seconds=monitored_device[const.TIMEOUT])

                if cur_time - last_contact > timeout:
                    log.info('device |%d| timed out', global_id)
                    to_delete.append(global_id)

            log.debug('Monitoring deleting timed out devices |%s|', str(to_delete))
            delete_dict = {
                const.GLOBAL_ID:{'$in': to_delete}
            }
            self.dev_coll.delete_many(delete_dict)
            self.connde_sensors.delete_many(delete_dict)
            self.connde_devices.delete_many(delete_dict)

            time.sleep(const.SERVER_MONITOR_SLEEP)

    def stop(self):
        log.info('Stop monitoring')
        self.monitor = False
