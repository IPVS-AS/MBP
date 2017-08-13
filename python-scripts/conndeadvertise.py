from advertiseservice.advertiseservice import AdvertiserService
from advertiseservice.advertiserclientlan import LanAdvertiser
from advertiseservice.advertiserclientbt import BTAdvertiser

import const

import argparse

import logging as log

if __name__ == '__main__':
    log.basicConfig(format='%(asctime)s |%(levelname)s|:%(message)s', level=log.DEBUG)

    parser = argparse.ArgumentParser(usage='%(prog)s [-h] [--lan|--bt]',
                                     description='Advertises this devices to a Connde Discovery Service. '
                                                 'What to advertise is read from the "autodeploy.json" file. '
                                                 'If a service was found and the registration was successfull, '
                                                 'received GLOBAL_IDs will be stored in the file "global_ids"')

    default_advertiser_class = LanAdvertiser

    parser.add_argument('--lan', help='Advertises this device over IP. This is the default.', dest=const.CLIENT_CMD_ARG,
                        action='store_const', const=LanAdvertiser)
    parser.add_argument('--bt', help='Advertises this device over Bluetooth', dest=const.CLIENT_CMD_ARG,
                        action='store_const', const=BTAdvertiser)

    args = vars(parser.parse_args())

    if not const.CLIENT_CMD_ARG in args:
        log.warning('No communication type specified. Using default ip')
        args[const.CLIENT_CMD_ARG] = default_advertiser_class

    advertise_service = AdvertiserService()
    try:
        advertise_service.start(args[const.CLIENT_CMD_ARG])
    except KeyboardInterrupt:
        advertise_service.stop()
