# Control Operator: Scripts for controlling a non-action actuator for tests

This folder contains operator scripts to control an actuator that does not trigger any functions. These can be used for testing IoT-Applications with the MBP's *Testing-Tool*.

## Hardware Setup 

 - a virtual machine (VM)

## Operator files 

 - `install.sh`: This file installs the necessary libraries to run the python script. 
 
 - `start.sh`: This file starts the execution of the python script.
 
 - `running.sh`: This file checks if the python script is running.
  
 - `stop.sh`: This file stops the execution of the python script.
 
 - `actuator_testingTool.py`: This python script contains a MQTT client, which subscribes to a MQTT topic in the form action/<actuator_id/#> to receive control commands. The actuator is only used for communication and does not execute any actions. 