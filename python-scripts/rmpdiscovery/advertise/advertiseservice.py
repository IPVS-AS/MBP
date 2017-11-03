import datetime
import json
import logging as log
import time

from . import advertiseconst as const


class AdvertiseService:
    """
    The AdvertiseService advertises a device in the local network and registers it at the RMP, if the discovery service is found.
    The AdvertiseService reads the configuration file (const.AUTODEPLOY_FILE).
    It triggers the discovery and registers all devices.
    The service maintains three dictionaries. All dictionaries are key value pairs, the keys being the LOCAL_ID of the devices.
        global_ids - stores the GLOBAL_ID assigned for the device
        last_keepalive - stores the tuple (last_contact, timeout)
        connected - stores the connection state
    """

    def __init__(self):
        """
        Initialize the AdvertiseService.
        Initialize the dictionaries and read the configuration file.
        """
        log.info('Setting up advertising service')
        self.global_ids = self._read_global_ids()
        self.last_keepalive = dict()
        self.connected = dict()

        self.autodeploy_data = self._read_autodeploy()
        if const.DEPLOY_SELF in self.autodeploy_data:
            self.host = self.autodeploy_data[const.DEPLOY_SELF]
        else:
            self.host = None
        log.info('Autodeploy data: ' + str(self.autodeploy_data))

        # calculate smallest timeout
        timeouts = [device[const.ADAPTER_CONF][const.TIMEOUT] for device in self.autodeploy_data[const.DEPLOY_DEVICES]]
        self.min_timeout = datetime.timedelta(seconds=min(timeouts)) / 2
        self.run = False

        self.set_unconnected()

    def start(self, AdvertiserClass):
        """
        Start the service. Network advertising depends on the specified AdvertiserClass.
        :param AdvertiserClass: the Advertiser used to advertise the device
        """
        log.info('Starting advertising service with |%s|', str(AdvertiserClass))

        advertiser = AdvertiserClass(self)  # init advertiser

        # advertise and register the device. Stop advertising after 5 unsuccessful tries
        tries = 0
        connected = False
        while not connected and tries < 5:
            tries += 1
            log.debug('advertising try |%d|', tries)
            advertiser.advertise()
            connected = self.check_connected()

        # the connection state might differ for the different devices
        connected_devices = [(key, self.global_ids) for key in self.connected if self.connected[key]]
        if len(connected_devices) == 0:
            log.info('No devices connected exiting')
            return
        else:
            log.info('Successfully connected |%d| devices', len(connected_devices))

        # send heartbeats for connected devices
        self.run = True
        while self.run:
            cur_time = datetime.datetime.utcnow()
            for device_name in self.last_keepalive.keys():
                if not self.connected[device_name]:
                    log.debug('Device |%s| not connected', device_name)
                    continue
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
                    advertiser.send_keep_alive(device_name, timeout.seconds)
                    self.last_keepalive[device_name][const.LAST_CONTACT] = cur_time

            time.sleep(self.min_timeout.total_seconds())

    def stop(self):
        """
        Stop the AdvertiseService and save the currently assigned GLOBAL_IDs.
        """
        log.info('Stopping advertising service')
        with open(const.GLOBAL_ID_FILE, 'w') as f:
            json.dump(self.global_ids, f)
        self.run = False

    def _read_autodeploy(self):
        """
        Read the local configuration file.
        :return the local configuraion
        :type: dict
        """
        try:
            json_string = open('/opt/rmpadvertise/' + const.AUTODEPLOY_FILE).read()
            deploy_data = json.loads(json_string)
            return deploy_data
        except IOError:
            return None
        except json.JSONDecodeError:
            log.exception('Illformated autodeploy file')

    def set_unconnected(self):
        """
        Fill the dictionaries and set all devices to state unconnected.
        :return:
        """
        if self.host is not None:
            self.connected[self.host[const.LOCAL_ID]] = False
            self.last_keepalive[self.host[const.LOCAL_ID]] = {
                const.TIMEOUT: datetime.timedelta(seconds=self.host[const.ADAPTER_CONF][const.TIMEOUT]),
                const.LAST_CONTACT: datetime.datetime.utcnow()
            }
            if self.host[const.LOCAL_ID] not in self.global_ids:
                self.global_ids[self.host[const.LOCAL_ID]] = 0

        for device in self.autodeploy_data[const.DEPLOY_DEVICES]:
            self.connected[device[const.LOCAL_ID]] = False
            self.last_keepalive[device[const.LOCAL_ID]] = {
                const.TIMEOUT: datetime.timedelta(seconds=device[const.ADAPTER_CONF][const.TIMEOUT]),
                const.LAST_CONTACT: datetime.datetime.utcnow()
            }
            if device[const.LOCAL_ID] not in self.global_ids:
                self.global_ids[device[const.LOCAL_ID]] = 0

    def check_connected(self):
        """
        Check if all devices are connected
        :return: True if all devices are connected, else return False
        :rtype: bool
        """
        connected = True
        if self.host is not None and not self.connected[self.host[const.LOCAL_ID]]:
            connected = False

        for device in self.autodeploy_data[const.DEPLOY_DEVICES]:
            if not self.connected.get(device[const.LOCAL_ID]):
                connected = False

        return connected

    def _read_global_ids(self):
        """
        Read the stored GLOBAL_IDs.
        :return: the stored IDs or a empty dictionary if the file could not be read.
        :rtype: dict
        """
        try:
            with open(const.GLOBAL_ID_FILE, 'r') as file:
                return json.load(file)
        except IOError:
            log.exception('Error opening global_ids file')
        except json.JSONDecodeError:
            log.exception('Illformated autodeploy file')

        return dict()
