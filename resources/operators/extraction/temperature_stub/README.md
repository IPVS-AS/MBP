# Extraction Operator: Scripts to extract temperature sensor data

This folder contains operator scripts to simulate the extraction of temperature sensor data. 

## Hardware Setup 

 - a computer running a Linux-based OS, such as a Raspberry Pi or a Laptop running the Ubuntu OS.

## Operator files 

 - `temperature_stub.py`: This python script contains a MQTT client, which publishes the simulated sensor data to a configured topic on the MBP.
 
 - `install.sh`: This file installs the necessary libraries to run the python script.
 
 - `start.sh`: This file starts the execution of the python script.
 
 - `running.sh`: This file checks if the python script is running.
  
 - `stop.sh`: This file stops the execution of the python script.