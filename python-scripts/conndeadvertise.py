from advertise.advertiseservice import AdvertiseService
from advertise.advertiserlan import LanAdvertiser
from advertise.advertiserbt import BTAdvertiser
from advertise import advertiseconst as const

import argparse
import signal

import logging as log


def stop_service(signum, frame):
    log.info('|%s| received. Shutting down...', str(signum))
    advertise_service.stop()


if __name__ == '__main__':
    log.basicConfig(format='%(asctime)s |%(levelname)s|:%(message)s', level=log.DEBUG)

    signal.signal(signal.SIGTERM, stop_service)
    signal.signal(signal.SIGINT, stop_service)

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

    if const.CLIENT_CMD_ARG not in args or args[const.CLIENT_CMD_ARG] is None:
        log.warning('No communication type specified. Using default ip')
        args[const.CLIENT_CMD_ARG] = default_advertiser_class

    advertise_service = AdvertiseService()
    try:
        advertise_service.start(args[const.CLIENT_CMD_ARG])
    except KeyboardInterrupt:
        stop_service('Keyboard interrupt', None)
