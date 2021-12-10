# Complex IoT data test operator: Scripts to simulate a sensor sending various double values in different json object and array strucutures

This folder contains operator scripts and a data model to deploy a sensor simulator which sends sensor values using various JSON object and array nestings.

## Hardware Setup 

 - A computer running a Linux-based OS, such as a Raspberry Pi or a Laptop running the Ubuntu OS. (Optionally, you can also use a virtual machine)

## Operator files 

 - `install.sh`: This file installs the necessary libraries to run the .py file.
 
 - `start.sh`: This file starts the execution of the .py file.
 
 - `running.sh`: This file checks if the .py file is running.
  
 - `stop.sh`: This file stops the execution of the .py file.
 
 - `nested_double_test.py`: This python script is responsible for connecting to the MBP via MQTT and sending the sensor values.

 - `dataModel.json`: This file contains a data model definition which can be used for the creation of a respective operator entity in the MBP.