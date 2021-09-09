# Extraction Operator: Scripts to extract temperature sensor data

This folder contains operator scripts to simulate the extraction of temperature sensor data. 

## Hardware Setup 

 - a computer running a Linux-based OS, such as a Raspberry Pi or a Laptop running the Ubuntu OS.

## Operator files 
 - `mbp_client.py`: This python script contains the logic to connect and to communicate with the MBP. It abstracts a MQTT client and further configuration steps.  
 - `entry-file-name`: This file contains solely the name of your main python script including its extension.  
 - `temperature_stub.py`: This python script simulates sensor data and uses the `mbp_client` to send these data to the MBP.  
 - `install.sh`: This file installs the necessary libraries to run the `mbp_client` and the main python script.  
 - `start.sh`: This file starts the execution of the main python script, the one indicated in the `entry-file-name`.  
 - `running.sh`: This file checks if the main python script is running.  
 - `stop.sh`: This file stops the execution of the main python script.
 - `dataModel.json`: This file contains a data model definition which can be used for the creation of a respective operator entity in the MBP.