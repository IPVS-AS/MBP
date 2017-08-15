from discovery.discoveryservice import DiscoveryService
from monitoringservice import MonitoringService

import logging as log
import time
import signal


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

    discovery_service = DiscoveryService()
    monitoring_service = MonitoringService()

    serving = True

    # bind socket for broadcasts
    try:
        discovery_service.start()
        monitoring_service.start()
    except KeyboardInterrupt:
        stop_services('keyboard interrupt', None)
