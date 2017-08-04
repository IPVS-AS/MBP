import time
import json
import socket as sck
# sudo pip3 install netifaces
import netifaces as ni
import const
from enum import auto
import bluetooth
import logging as log
from advertiserclientlan import LanAdvertiser
from advertiserclientbt import BTAdvertiser


class AdvertiserService:
    def __init__(self):
        log.info('Setting up advertising service')
        self.global_ids = dict()

        self.autodeploy_data = self.read_autodeploy()
        if const.DEPLOY_SELF in self.autodeploy_data:
            self.host = self.autodeploy_data[const.DEPLOY_SELF]
        else:
            self.host = None
        log.info('Autodeploy data: ' + str(self.autodeploy_data))

        self.set_unconnected()

    def start(self, AdvertiserClass):
        log.info('Starting advertising service with |%s|', str(AdvertiserClass))

        advertiser = AdvertiserClass(self)

        while not self.check_connected():
            advertiser.advertise()

        log.info('Successfully connected |%d| devices', len(self.autodeploy_data[const.DEPLOY_DEVICES]))

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
        # TODO client - difference between sensors and adapters? Treat all as devices?
        # TODO client - replace const.NAME with const.LOCAL_ID
        if self.host is not None:
            self.global_ids[self.host[const.NAME]] = 0

        for device in self.autodeploy_data[const.DEPLOY_DEVICES]:  # set invalid id for all devices
            self.global_ids[device[const.NAME]] = 0

    def check_connected(self):
        # TODO client - must be able to tell the difference between (un-)successfull connect and reconnect (reconnect already has a valid global_id)
        connected = True
        if self.host is not None and not self.global_ids[self.host[const.NAME]]:
            connected = False

        for device in self.autodeploy_data[const.DEPLOY_DEVICES]:
            if not self.global_ids.get(device[const.NAME]):
                connected = False

        return connected


if __name__ == '__main__':
    log.basicConfig(format='%(asctime)s |%(levelname)s|:%(message)s', level=log.DEBUG)

    advertise_service = AdvertiserService()
    advertise_service.start(LanAdvertiser)
