# Arduino-based switch adapter

This folder contains adapter scripts to control a RF-switch. The hardware setup for this adapter corresponds to:

 - a Raspberry Pi
 - an Arduino Nano connected to the Raspberry Pi via USB (serial) and connected to a 433 MHz transmitter (see rflaster in https://github.com/timwaizenegger/rfblaster)
 - a 433 MHz plug receiver

The following files are provided in this folder:
 
 - `rf-switch/rfswitch.ino`: This file contains the Arduino code that listen to its serial port for 'ON'/'OFF' commands and emits RF signals to a RF receiver. For more information, see  https://github.com/timwaizenegger/rfblaster.
 
 - `actuatoradapter_switch_hw.py`: This file contains a MQTT client, which subscribes to a configured topic to receive ON/OFF commands. These commands are forwarded to the Arduino through USB (serial connection).
 
 - `install.sh`: This file installs the necessary libraries to run the python script. Limitation: flashing the rf-switch code to the Arduino is **not yet** done.
 
 - `start.sh`: This file starts the execution of the python script.
 
 - `running.sh`: This file checks if the python script is running.
  
 - `stop.sh`: This file stops the execution of the python script.