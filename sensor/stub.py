import sys
from random import randint
from sensorbase import SensorBase


class SensorStub (SensorBase):

    def __init__(self, argv):
        super().__init__(argv = argv, periodic = True) # python 3 only

    def _getSensorValue(self):
        return randint(0,9)

class SensorWithCallback (SensorBase):

    def __init__(self, argv, periodic = False):
        super().__init__(argv = argv, periodic = False) # python 3 only

    def myCallbackFunction(self, value):
        self._gotSensorValue(value)

class ActuatorStub (SensorBase):

    def __init__(self, argv, actuator = True, periodic = False):
        super().__init__(argv = argv,  actuator = True,  periodic = False) # python 3 only

    def _setActuatorValue(self, val):
        print (val.payload)
        print (val.value)

if __name__ == "__main__":
    client = SensorStub(sys.argv)
    client.run()