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
        log.info('Autodeploy data: ' + str(self.autodeploy_data))

        self.set_unconnected()

    def start(self, AdvertiserClass):
        log.info('Starting advertising service with |%s|', str(AdvertiserClass))

        advertiser = AdvertiserClass(self)

        while not self.check_connected():
            advertiser.advertise()

    def read_autodeploy(self):
        try:
            json_data = open(const.AUTODEPLOY_FILE).read()
            return json.loads(json_data)
        except IOError:
            return None

    def set_unconnected(self):
        for sensor in self.autodeploy_data[const.DEPLOY_SENSORS]:  # set invalid id for all devices
            self.global_ids[sensor[const.NAME]] = -1

        for actuator in self.autodeploy_data[const.DEPLOY_ADAPTERS]:
            self.global_ids[actuator[const.NAME]] = -1

    def check_connected(self):
        connected = True
        for sensor in self.autodeploy_data[const.DEPLOY_SENSORS]:
            if self.global_ids.get(sensor[const.NAME]) == -1:
                connected = False

        for actuator in self.autodeploy_data[const.DEPLOY_ADAPTERS]:
            if self.global_ids.get(actuator[const.NAME]) == -1:
                connected = False

        return connected


if __name__ == '__main__':
    log.basicConfig(format='%(asctime)s |%(levelname)s|:%(message)s', level=log.DEBUG)

    advertise_service = AdvertiserService()
    advertise_service.start(LanAdvertiser)

