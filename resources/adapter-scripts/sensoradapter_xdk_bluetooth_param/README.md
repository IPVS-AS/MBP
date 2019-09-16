# XDK Bluetooth adapter

The following parameters need to be provided on deployment:

 - `"mac" (text)`: XDK's MAC address

This folder contains adapter scripts for reading light sensor values from a Bosch XDK device via Bluetooth. The hardware setup for this adapter corresponds to:

 - a Raspberry Pi 3B (Stretch or Buster) configured as Bluetooth Hotspot
 - a XDK Bosch device running the BLE project 

The following files are provided in this folder:
 
 - `XDK_BLE.zip`: This file contains the Bluetooth Low Energy Project which should be import to XDK.
 
 - `sensoradapter_xdk_bluetooth_param.py`: This file contains a MQTT client, which publishes XDK sensor values to a configured topic on MBP Broker and contains the bluetooth implementation to receive XDK sensor values.

 - `install.sh`: This file installs the necessary libraries to run the python script.
 
 - `start.sh`: This file starts the execution of the python script.
 
 - `running.sh`: This file checks if the python script is running.
  
 - `stop.sh`: This file stops the execution of the python script.

## XDK Device Setup

The XDK Workbench version used is 3.6.0.

To import/export a project to XDK device see https://developer.bosch.com/web/xdk/importing-a-project


