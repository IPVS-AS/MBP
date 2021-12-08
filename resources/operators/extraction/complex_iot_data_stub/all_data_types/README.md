# Complex IoT data test operator: Scripts to simulate a sensor sending all available MBP data types

This folder contains operator scripts and a data model to deploy a sensor simulator which sends sensor values using all data types of the MBP.

## Hardware Setup 

 - A computer running a Linux-based OS, such as a Raspberry Pi or a Laptop running the Ubuntu OS. (Optionally, you can also use a virtual machine)

## Operator files 

 - `install.sh`: This file installs the necessary libraries to run the .py file.
 
 - `start.sh`: This file starts the execution of the .py file.
 
 - `running.sh`: This file checks if the .py file is running.
  
 - `stop.sh`: This file stops the execution of the .py file.
 
 - `test_all_mbp_data_types.py`: This python script is responsible for connecting to the MBP via MQTT and sending the sensor values.

 - `dataModel.json`: This file contains a data model definition which can be used for the creation of a respective operator entity in the MBP.