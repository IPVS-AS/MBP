import logging as log
import abc
import time
from random import randint
from datetime import datetime
from queue import Queue

try:
    import spidev
except ImportError:
    log.debug('local_settings failed to import spidev', exc_info=True)
try:
    import RPi.GPIO as GPIO
except ImportError:
    log.debug('local_settings failed to import GPIO', exc_info=True)

from stoppable import StoppableThread

class SensorBase (StoppableThread):
    
    def __init__(self, sensor_id, queue, sleeptime, daemon = False):
        super().__init__() # python 3 only
        self.__sensor_id = sensor_id
        self.__queue = queue # value storing queue
        self.__sleeptime = sleeptime # time between each value read
        self.daemon = daemon

    @staticmethod
    def readadc(adcnum):
        spi = spidev.SpiDev()
        spi.open(0,0)
        # read SPI data from MCP3004 chip, 4 possible adc’s (0 thru 3)
        if ((adcnum > 3) or (adcnum < 0)):
            return -1
        r = spi.xfer2([1,8 + adcnum <<4,0])
        #print(r)
        adcout = ((r[1] & 3) << 8) + r[2]
        return adcout

    def run(self):
        # reads sensor value and adds to queue
        while (not self.stopped()):
            value = self._getSensorValue()
            self.notify_observers(value)
            time.sleep(self.__sleeptime)

    @abc.abstractmethod
    def _getSensorValue(self):
        # CODE TO GET SENSOR VALUE
        pass

    def notify_observers(self, value):
        # adds value to the queue
        dict_ = {
            "id": self.__sensor_id,
            "time": datetime.now(),
            "value": value,
        }
        self.__queue.put_nowait(dict_)


class SensorStub (SensorBase):

    def __init__(self, sensor_id, queue, sleeptime, daemon):
        super().__init__(sensor_id, queue, sleeptime, daemon) # python 3 only

    def _getSensorValue(self):
        return randint(0,9)

sleeptime = 1
threads = []

def signal_handler(signal, frame):
    print ("Closing all threads...")
    for t in threads:
        if t.isAlive():
            t.stop()
    for t in threads:
        if t.isAlive():
            t.join()
    print ("Finished.")
    sys.exit(0)

if __name__ == "__main__":
    queue_ = Queue(0) # unlimited size
    stub = SensorStub("stub", queue_, sleeptime, daemon=True) # still having troubles to stop threads - use daemon thread
    threads.append(stub)
    stub.start()

    while (True):
        print (queue_.get(block=True))