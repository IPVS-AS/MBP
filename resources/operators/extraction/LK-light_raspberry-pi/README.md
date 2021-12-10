# Extraction Operator: Scripts to extract LK light sensor data

This folder contains operator scripts to extract sensor data from a LK light sensor :partly_sunny: . 

## Hardware Setup 

- a Raspberry Pi.

- a [LK base board](http://www.linkerkit.de/index.php?title=LK-Base-RB_2) or an arbitrary AD converter.

- a [LK light sensor](http://www.linkerkit.de/index.php?title=LK-Light-Sen) plugged to the `A2 analog input` of the LK base board.  :warning: If other input is used, this needs to be configured through the parameter `channel (number)`. 

  This sensor is a light dependent resistor (LDR), the resistance decreases when light intensity increases. 

  Light resistance: 20KΩ 

  Dark resistance: 1MΩ

## Parameters

Optional parameter:

* `"interval" (number)`: the desired time interval between two sensor measurements in seconds. If this is not provided, the default time of `30 seconds`is used.
* `"channel" (number)`: the analog input channel, which can be `0`, `1`, `2`(default) or `3`. 

## Operator files 

 - `mbp_client.py`: This python script contains the logic to connect and to communicate with the MBP. It abstracts a MQTT client and further configuration steps.  
 - `mbp_analog_reader.py`: This python script contains the logic to read analog sensor values.
 - `entry-file-name`: This file contains solely the name of the user-defined main python script including its extension. 

- `LK-light_raspberry-pi.py`: This script communicates with the LK sensor and uses the `mbp_client` to send data to the MBP. 
- `install.sh`: This file installs the necessary libraries to run the python script.
- `start.sh`: This file starts the execution of the python script.
- `running.sh`: This file checks if the python script is running.
- `stop.sh`: This file stops the execution of the python script.
- `dataModel.json`: This file contains a data model definition which can be used for the creation of a respective operator entity in the MBP.