# Extraction Operator: Scripts to extract LK light sensor data

This folder contains operator scripts to extract sensor data from a LK light sensor. 

## Hardware Setup 

- a Raspberry Pi.
- a [LK base board](http://www.linkerkit.de/index.php?title=LK-Base-RB_2) or an arbitrary AD converter.
- a [LK light sensor](http://www.linkerkit.de/index.php?title=LK-Light-Sen) plugged to the `A2 analog input` of the LK base board. This sensor is a light dependent resistor (LDR), i.e., the resistence decreases when light intensity increases.

## Operator files 

- `LK-light_raspberry-pi.py`: This python script contains a MQTT client, which publishes sensor data to a configured topic on the MBP.
 
- `install.sh`: This file installs the necessary libraries to run the python script.
 
- `start.sh`: This file starts the execution of the python script.
 
- `running.sh`: This file checks if the python script is running.
  
- `stop.sh`: This file stops the execution of the python script.