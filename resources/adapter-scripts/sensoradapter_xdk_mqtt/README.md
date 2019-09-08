This folder contains adapter scripts for reading values from a bunch of sensors from a Bosch XDK device via MQTT protocol. The hardware setup for this adapter corresponds to:

 - a Raspberry Pi 3B (Stretch or Buster) configured as WiFi Hotspot
 - a XDK Bosch device running the MQTT project 

The following parameters need to be provided on deployment:

 - `"sensor" (text)`: name of sensor which want to read the values. E.g.:  "humidity"/"temperature"/"light"

The following files are provided in this folder:
 
 - `XDK_MQTT.zip`: This file contains the Http Protocol Project which should be import to XDK.
 
 - `sensoradapter_xdk_mqtt.py`: This file contains a MQTT client, which publishes XDK sensor values to a configured topic on MBP Broker received through a topic which the script subscribes on the localhost to receive the json containing all the sensors enabled and its values on the XDK device

 - `install.sh`: This file installs the necessary libraries to run the python script.
 
 - `start.sh`: This file starts the execution of the python script.
 
 - `running.sh`: This file checks if the python script is running.
  
 - `stop.sh`: This file stops the execution of the python script.

## XDK Device Setup

The XDK Workbench version used is 3.6.0.

To import/export a project to XDK device see https://developer.bosch.com/web/xdk/importing-a-project

## Script details

The script `sensoradapter_xdk.py` subscribes on the localhost broker to receive a JSON containing all the sensors enabled and its values on the XDK device...

Example of JSON received:
```
{ 
    "accelerometer"  : { "x" : "-20",  "y" : "-14", "z" : "1014"}, 
    "gyro" : { "x" : "122",  "y" : "61", "z" : "122"},
    "magnetometer"  : { "x": "22",  "y" : "-17", "z" : "-58", "r" : "6739"}, 
    "humidity" : "31",
    "pressure" : "96725",
    "light" : "25920",
    "temperature" : "25.551000"
}
```