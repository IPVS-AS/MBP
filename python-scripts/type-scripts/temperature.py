import sys
from random import randint
from sensorbase import SensorBase

import spidev
import RPi.GPIO as GPIO

class TemperatureSensor (SensorBase):

    def __init__(self, argv, periodic = True):
        super().__init__(argv = argv, periodic = True) # python 3 only

    def _getSensorValue(self):
        value = readadc(self.getPinset()[0]) 
        volts = (value * 3.3) / 1024
        temperature_C = (volts - 0.5) * 100
        temperature_F = (temperature_C * 9 / 5) + 32
        print (temperature_C)
        return temperature_C

if __name__ == "__main__":
    client = TemperatureSensor(sys.argv)

    spi = spidev.SpiDev()
    spi.open(0,0)

    def readadc(adcnum):
        # read SPI data from MCP3004 chip, 4 possible adc's (0 thru 3)
        if adcnum > 3 or adcnum < 0:
            return -1
        r = spi.xfer2([1,8+adcnum <<4,0])
        adcout = ((r[1] &3) <<8)+r[2]
        return adcout

    client.run()