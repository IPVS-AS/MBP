import sys
from random import randint
from sensorbase import SensorBase

import RPi.GPIO as GPIO

class FlameSensor (SensorBase):

    def __init__(self, argv, periodic = False):
        super().__init__(argv = argv, periodic = False) # python 3 only

if __name__ == "__main__":
    client = FlameSensor(sys.argv)

    def eventFlame(e):
        print ('flame')
        client._gotSensorValue(1)

    GPIO.setwarnings(False)
    GPIO.setmode(GPIO.BCM)
    GPIO.setup(client.getPinset()[0], GPIO.IN, pull_up_down=GPIO.PUD_DOWN)
    GPIO.add_event_detect(client.getPinset()[0], GPIO.RISING, bouncetime = 200, callback = eventFlame)

    client.run()