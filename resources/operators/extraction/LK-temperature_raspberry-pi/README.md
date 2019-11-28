# Extraction Operator: Scripts to extract LK temperature sensor data

This folder contains operator scripts to extract sensor data from a LK temperature sensor. 

## Hardware Setup 

 - a Raspberry Pi
 - a [LK base board](http://www.linkerkit.de/index.php?title=LK-Base-RB_2) or an arbitrary AD converter
 - a [LK temperature sensor](http://www.linkerkit.de/index.php?title=LK-Temp) plugged to the `A0 analog input` of the LK base board

## Operator files 

 - `LK-temperature_raspberry-pi.py`: This python script contains a MQTT client, which publishes sensor data to a configured topic on the MBP.
 
 - `install.sh`: This file installs the necessary libraries to run the python script.
 
 - `start.sh`: This file starts the execution of the python script.
 
 - `running.sh`: This file checks if the python script is running.
  
 - `stop.sh`: This file stops the execution of the python script.