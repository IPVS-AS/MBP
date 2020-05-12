# Monitoring Operator: Scripts to read CPU usage

This folder contains operator scripts to read CPU usage percentage (%) values of Raspberry Pis and publish them on a MQTT topic. By running the installation script, the command line tool `mpstat` is installed on the device which is then used by the python script in order to determine the CPU usage. For each single value, the CPU idle time percentage is collected for an interval of 15 seconds. Finally the CPU usage is calculated by applying `(1 - idle time percentage)`.

## Hardware Setup 

 - a Raspberry Pi

## Operator files 

 - `cpu-usage_raspberry-pi.py`: This python script contains a MQTT client, which publishes CPU usage data to a configured topic on the MBP.
 
 - `install.sh`: This file installs the necessary libraries to run the python script.
 
 - `start.sh`: This file starts the execution of the python script.
 
 - `running.sh`: This file checks if the python script is running.
  
 - `stop.sh`: This file stops the execution of the python script.