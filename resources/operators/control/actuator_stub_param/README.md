# Control Operator: Scripts to simulate controling an actuator

This folder contains Parametrized operator scripts to simulate controlling an actuator.

## Hardware Setup 

- a computer running a Linux-based OS, such as a Raspberry Pi or a Laptop running the Ubuntu OS.

## Parameters

The following parameters need to be provided on deployment:

 - `"File_Name" (Text)`: The file name (e.g., "my-actions.txt"), which is used to write messages from rule actions, for example from the button `Test action` in the `Rule Actions overview`.

## Operator files 

- `actuator_stub_param.py`: This python script contains a MQTT client, which subscribes to a MQTT topic in the form `action/<actuator_id/#>` to receive control commands. The received actions are logged in the file `actions.txt` on the home folder of the IoT device running this script. 
 
- `install.sh`: This file installs the necessary libraries to run the python script.
 
- `start.sh`: This file starts the execution of the python script.
 
- `running.sh`: This file checks if the python script is running.
  
- `stop.sh`: This file stops the execution of the python script.