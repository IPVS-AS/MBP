The approach below leads with the scenario where the ESP12E-8266 is connected to a WiFi Hotspot deployed on a Raspberry Pi 3B that is connected to the internal network.

# ESP12E-8266 adapter

The following parameters need to be provided on deployment:

 - `"esp" (text)`: esp server ip

This folder contains adapter scripts for reading temperature sensor values from a esp12e-8266 device via HTTP. The hardware setup for this adapter corresponds to:

 - a Raspberry Pi 3B (Stretch or Buster) configured as WiFi Hotspot
 - a esp8266 Microcontroller with esp-12e module

The following files are provided in this folder:
 
 - `adapter/adapter.ino`: This file contains the arduino project which esp8266 must run.
 
 - `sensoradapter_esp.py`: This file contains the implementation to send the MBP MQTT broker configuration to esp12e device through http push approach.

 - `install.sh`: This file installs the necessary libraries to run the python script.
 
 - `start.sh`: This file starts the execution of the python script.
 
 - `running.sh`: This file checks if the python script is running.
  
 - `stop.sh`: This file stops the execution of the python script.

# Push approach details

This approach is implemented as following:

 - ESP12E-8266 runs a http server.
 - MBP deploys the scripts on the WiFi Hotspot device.
 - IP of ESP12E-8266 device needed as parameter.
 - Hotspot device sends a http post request to the server running on ESP12E-8266 containing broker configuration.
 - Hotspot device sends a http post request to start the application.
 - ESP12E-8266 once having the MBP broker configuration starts sending sensor's values.
 - When python script is killed on the Hotspot device sends a http post request to end the application.


