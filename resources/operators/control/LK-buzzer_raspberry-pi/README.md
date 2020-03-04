# Control Operator: Scripts to trigger a LK buzzer

This folder contains operator scripts to trigger a LK buzzer actuator. 

## Hardware Setup 

- a Raspberry Pi.  
- (optional) a [LK base board](http://www.linkerkit.de/index.php?title=LK-Base-RB_2).  
- a [LK buzzer actuator](http://www.linkerkit.de/index.php?title=LK-Buzzer) plugged to the `digital pin GPIO15` of the Raspberry Pi or of the LK base board.

## Operator files 

- `LK-buzzer_raspberry-pi.py`: This python script contains a MQTT client, which subscribes to a configured topic on the MBP.
 
- `install.sh`: This file installs the necessary libraries to run the python script.
 
- `start.sh`: This file starts the execution of the python script.
 
- `running.sh`: This file checks if the python script is running.
  
- `stop.sh`: This file stops the execution of the python script.