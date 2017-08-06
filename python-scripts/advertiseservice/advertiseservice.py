import datetime
import json
import logging as log
import time

import const


class AdvertiserService:
    def __init__(self):
        log.info('Setting up advertising service')
        self.global_ids = dict()
        self.last_keepalive = dict()

        self.autodeploy_data = self.read_autodeploy()
        if const.DEPLOY_SELF in self.autodeploy_data:
            self.host = self.autodeploy_data[const.DEPLOY_SELF]
        else:
            self.host = None
        log.info('Autodeploy data: ' + str(self.autodeploy_data))

        # calculate smallest timeout
        timeouts = [device[const.ADAPTER_CONF][const.TIMEOUT] for device in self.autodeploy_data[const.DEPLOY_DEVICES]]
        self.min_timeout = datetime.timedelta(seconds=min(timeouts)) / 2

        self.set_unconnected()

    def start(self, AdvertiserClass):
        log.info('Starting advertising service with |%s|', str(AdvertiserClass))

        advertiser = AdvertiserClass(self)

        while not self.check_connected():
            advertiser.advertise()

        log.info('Successfully connected |%d| devices', len(self.autodeploy_data[const.DEPLOY_DEVICES]))

        while True:
            cur_time = datetime.datetime.utcnow()
            for device_name in self.last_keepalive.keys():
                last_contact = self.last_keepalive[device_name][const.LAST_CONTACT]
                timeout = self.last_keepalive[device_name][const.TIMEOUT]
                passed_time = cur_time - last_contact
                max_passed_time = timeout - self.min_timeout
                log.debug('Checking device |%s|. '
                          '\nTimeout is |%s|, min timeout is |%s|, max passed time is |%s|,'
                          '\nlast contact at |%s|, now is |%s|, passed time is |%s|,'
                          '\nneed keep alive |%s|',
                          device_name, str(timeout), str(self.min_timeout), str(max_passed_time), str(last_contact),
                          str(cur_time), str(passed_time), str(passed_time >= max_passed_time))
                if passed_time >= max_passed_time:
                    advertiser.send_keep_alive(device_name)
                    self.last_keepalive[device_name][const.LAST_CONTACT] = cur_time

            time.sleep(self.min_timeout.total_seconds())

    def stop(self):
        log.info('Stopping advertising service')

    def read_autodeploy(self):
        try:
            json_string = open(const.AUTODEPLOY_FILE).read()
            deploy_data = json.loads(json_string)
            return deploy_data
        except IOError:
            return None
        except json.JSONDecodeError:
            log.exception('Illformated autodeploy file')

    def set_unconnected(self):
        # TODO client - replace const.NAME with const.LOCAL_ID
        if self.host is not None:
            self.global_ids[self.host[const.NAME]] = 0

        for device in self.autodeploy_data[const.DEPLOY_DEVICES]:  # set invalid id for all devices
            self.global_ids[device[const.NAME]] = 0
            self.last_keepalive[device[const.NAME]] = {
                const.TIMEOUT: datetime.timedelta(seconds=device[const.ADAPTER_CONF][const.TIMEOUT]),
                const.LAST_CONTACT: datetime.datetime.utcnow()
            }

    def check_connected(self):
        # TODO client - must be able to tell the difference between (un-)successfull connect and reconnect (reconnect already has a valid global_id)
        connected = True
        if self.host is not None and not self.global_ids[self.host[const.NAME]]:
            connected = False

        for device in self.autodeploy_data[const.DEPLOY_DEVICES]:
            if not self.global_ids.get(device[const.NAME]):
                connected = False

        return connected
