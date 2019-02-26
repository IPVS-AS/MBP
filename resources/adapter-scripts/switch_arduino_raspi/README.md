# Arduino-based switch adapter

This folder contains adapter scripts to control a RF-switch. The hardware setup for this adapter corresponds to:

 - a Raspberry Pi
 - an Arduino Nano connected to the Raspberry Pi via USB (serial) and connected to 433 MHz transmitter (see rflaster in https://github.com/timwaizenegger/rfblaster)
 - a 433 MHz plug receiver

The following files are provided in this folder:
 
 - `rf-switch/rfswitch.ino`: This file contains the Arduino code that listen to its serial port for 'ON'/'OFF' commands and emits RF signals to a RF receiver. For more information, see  https://github.com/timwaizenegger/rfblaster.
 
 - `actuatoradapter_switch_hw.py`: This file contains a MQTT client, which subscribes to a configured topic to receive ON/OFF commands. These commands are forwarded to the Arduino through USB (serial connection).