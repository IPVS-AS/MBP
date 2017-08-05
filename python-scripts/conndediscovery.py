from discoveryservice import DiscoveryService
from monitoringservice import MonitoringService

import logging as log
import time

# main
if __name__ == "__main__":
    log.basicConfig(format='%(asctime)s |%(levelname)s|:%(message)s', level=log.DEBUG)

    discovery_service = DiscoveryService()
    monitoring_service = MonitoringService()

    # bind socket for broadcasts
    try:
        discovery_service.start()
        monitoring_service.start()
        while True:
            print('.', end='', flush=True)
            time.sleep(5)
    except KeyboardInterrupt:
        log.info('Keyboard interrupt. Shutting down...')
        monitoring_service.stop()
        discovery_service.stop()
