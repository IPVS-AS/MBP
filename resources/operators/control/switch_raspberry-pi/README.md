# Control Operator: Scripts to control an Arduino-based Switch

This folder contains operator scripts to control a radio frequency (RF) and Arduino-based switch.

## Hardware Setup 

 - a Raspberry Pi
 - an Arduino Nano connected to the Raspberry Pi via USB (serial)
 - a 433 MHz transmitter connected to the Arduino (see rflaster in https://github.com/timwaizenegger/rfblaster)
 - a 433 MHz plug receiver

## Operator files 

 - `rf-switch/rfswitch.ino`: This file contains the Arduino code that listen to its serial port for 'ON'/'OFF' commands and emits RF signals to the RF plug receiver. For more information, see  https://github.com/timwaizenegger/rfblaster.
 
 - `actuatoradapter_switch_hw.py`: This python script contains a MQTT client, which subscribes to a configured topic to receive ON/OFF commands. These commands are forwarded to the Arduino through USB (serial connection).
 
 - `install.sh`: This file installs the necessary libraries to run the python script. Limitation: flashing the rf-switch code to the Arduino is **not yet** done automatically, that is, users need to flash the Arduino code themselves.
 
 - `start.sh`: This file starts the execution of the python script.
 
 - `running.sh`: This file checks if the python script is running.
  
 - `stop.sh`: This file stops the execution of the python script.