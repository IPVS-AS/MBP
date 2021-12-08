# Extraction Operator: Parametrized scripts to extract temperature sensor data

This folder contains operator scripts to simulate the extraction of temperature sensor data. The scripts take input parameters upon their deployment.

## Parameters

The following parameters need to be provided on deployment:

 - `"interval" (number)`: The time interval between two temperature measures in seconds.

## Hardware Setup 

 - a computer running a Linux-based OS, such as a Raspberry Pi or a Laptop running the Ubuntu OS.

## Operator files 

 - `temperature_stub_param.py`: This python script contains a MQTT client, which publishes the simulated sensor data to a configured topic on the MBP.
 
 - `install.sh`: This file installs the necessary libraries to run the python script.
 
 - `start.sh`: This file starts the execution of the python script.
 
 - `running.sh`: This file checks if the python script is running.
  
 - `stop.sh`: This file stops the execution of the python script.

 - `dataModel.json`: This file contains a data model definition which can be used for the creation of a respective operator entity in the MBP.