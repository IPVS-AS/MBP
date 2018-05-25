import argparse
import logging as log
import signal

from rmpdiscovery.discovery import discoveryconst as const
from rmpdiscovery.discovery.discoveryservice import DiscoveryService
from rmpdiscovery.discovery.monitoringservice import MonitoringService


def stop_services(signum, frame):
    log.info('|%s| received. Shutting down...', str(signum))
    global serving
    serving = False
    monitoring_service.stop()
    discovery_service.stop()


# main
if __name__ == "__main__":
    log.basicConfig(format='%(asctime)s |%(levelname)s|:%(message)s', level=log.DEBUG)

    signal.signal(signal.SIGTERM, stop_services)
    signal.signal(signal.SIGINT, stop_services)

    parser = argparse.ArgumentParser(usage='%(prog)s [-h] [--lan|--bt]',
                                     description='Starts the RMP\'s discovery service.'
                                                 'Devices may discover and use the service to register at the RMP'
                                                 'The gateways to start my be specified using command line parameters.'
                                                 'By default no gateway is started')

    parser.add_argument('--lan', help='Starts the discovery gateway for IP-based networks.', dest=const.SERVER_CMD_LAN,
                        action='store_const', const=True, default=False)
    parser.add_argument('--bt', help='Starts the discovery gateway for Bluetooth networks', dest=const.SERVER_CMD_BT,
                        action='store_const', const=True, default=False)

    discovery_service = DiscoveryService()
    monitoring_service = MonitoringService()

    args = vars(parser.parse_args())

    serving = True

    # bind socket for broadcasts
    try:
        discovery_service.start(lan=args[const.SERVER_CMD_LAN], bt=args[const.SERVER_CMD_BT])
        monitoring_service.start()
    except KeyboardInterrupt:
        stop_services('keyboard interrupt', None)
