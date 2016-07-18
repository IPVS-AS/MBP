import sys
from random import randint
from sensorbase import SensorBase


class SensorStub (SensorBase):

    def __init__(self, argv, sleeptime = 1):
        super().__init__(argv = argv, sleeptime = sleeptime, periodic = True) # python 3 only

    def _getSensorValue(self):
        return randint(0,9)

class SensorWithCallback (SensorBase):

    def __init__(self, argv, sleeptime = 1, periodic = False):
        super().__init__(argv = argv,  sleeptime = sleeptime,  periodic = False) # python 3 only

    def myCallbackFunction(self, value):
        self._gotSensorValue(value)

if __name__ == "__main__":
    client = SensorStub(sys.argv)