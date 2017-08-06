from advertiseservice.advertiseservice import AdvertiserService
from advertiseservice.advertiserclientlan import LanAdvertiser

import logging as log

if __name__ == '__main__':
    log.basicConfig(format='%(asctime)s |%(levelname)s|:%(message)s', level=log.DEBUG)

    advertise_service = AdvertiserService()
    try:
        advertise_service.start(LanAdvertiser)
    except KeyboardInterrupt:
        advertise_service.stop()
