# Monitoring Operator: Scripts to read CPU temperature

This folder contains operator scripts to read CPU temperature (°C) of Raspberry Pis. 

## Hardware Setup 

 - a Raspberry Pi

## Operator files 

 - `cpu-temperature_raspberry-pi.py`: This python script contains a MQTT client, which publishes CPU temperature data to a configured topic on the MBP.
 
 - `install.sh`: This file installs the necessary libraries to run the python script.
 
 - `start.sh`: This file starts the execution of the python script.
 
 - `running.sh`: This file checks if the python script is running.
  
 - `stop.sh`: This file stops the execution of the python script.