import sys

import RPi.GPIO as GPIO

from sensorbase import SensorBase


class DoubleButtonSensor(SensorBase):
    def __init__(self, argv, periodic=False):
        super().__init__(argv=argv, periodic=False)  # python 3 only


if __name__ == "__main__":
    client = DoubleButtonSensor(sys.argv)


    def eventB1(e):
        print('Button 1 pressed')
        client._gotSensorValue(1)


    def eventB2(e):
        print('Button 2 pressed')
        client._gotSensorValue(2)


    GPIO.setwarnings(False)
    GPIO.setmode(GPIO.BCM)
    GPIO.setup(client.getPinset()[0], GPIO.IN, pull_up_down=GPIO.PUD_DOWN)
    GPIO.setup(client.getPinset()[1], GPIO.IN, pull_up_down=GPIO.PUD_DOWN)
    GPIO.add_event_detect(client.getPinset()[0], GPIO.RISING, bouncetime=200, callback=eventB1)
    GPIO.add_event_detect(client.getPinset()[1], GPIO.RISING, bouncetime=200, callback=eventB2)

    client.run()
