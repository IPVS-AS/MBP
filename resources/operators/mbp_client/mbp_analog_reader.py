#!/usr/bin/env python3
import spidev

"""
Analog input reader (from linker-base Analog-Digital Converter (ADC)
In the Raspi, reading from the Serial Peripherical Interface (spi) needs to be allowed
"""
class AnalogInputReader(object):

  def __init__(self, adc_channel):
    self.spi = spidev.SpiDev()
    self.spi.open(0,0) # (bus, device)
    # REQUIRED to set speed, otherwise might not work due to a bug in spidev:
    self.spi.max_speed_hz = 1000000 # 1 MHz for SPI
    self.adc_channel = adc_channel

  def read_adc(self):
    # read SPI data from MCP3008 chip, 8 possible adc’s (0 thru 7)
    if ((self.adc_channel > 7) or (self.adc_channel < 0)):
      return -1
    r = self.spi.xfer2([1,8+self.adc_channel <<4,0])
    adcout = ((r[1] &3) <<8)+r[2]
    return adcout

  def get_level(self):
    value = self.read_adc()
    volts = (value*3.3)/1024
    return (volts, value)

  def read_temperature(self):
    v0 = self.get_level()
    temperature = ((v0[0] * 100) - 50) # celsius
    return temperature

  def read_light(self):
    """light dependent resistor (LDR): resistence of the light sensor decreases when light intensity increases"""
    v0 = self.get_level()
    if v0[1] > 0:
      resistenceSensor = ((1023 - v0[1])*10)/v0[1]
      return resistenceSensor
    else:
      return 0

  def read_sound(self):
    v0 = self.get_level()
    return v0[1] 