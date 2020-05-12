# Control Operator: Scripts to control a LED RGB from LinkerKit

This folder contains operator scripts to control a [LinkerKit LED RGB](http://www.linkerkit.de/index.php?title=LK-LED-RGB) actuator.

## Hardware Setup 

- a Raspberry Pi.  
- a [LinkerKit LED RGB](http://www.linkerkit.de/index.php?title=LK-LED-RGB) plugged to the GPIO of the Raspberry Pi.

## LED Setup

- to install the necessary libraries follow installation on Raspberry Pi [instructions](http://www.linkerkit.de/images/2/24/LK-LED-RGB_17-05-2017.pdf)
- then the LED needs to be plugged to the corresponding GPIO of the Raspberry Pi.

More details can be found here: [LK-LED-RGB](http://www.linkerkit.de/index.php?title=LK-LED-RGB)

## Operator files 

- `led_adapter.py`: This python script contains a MQTT client, which subscribes to a configured topic on the MBP. When a message is received on the subscribed topic it triggers the LED action and then the LED color is changed. To choose the color is needed to send a JSON when creating a **rule action** (on *Additional data field*) on the platform (e.g.: {'color': 'RED'} ). Only RED, GREEN and BLUE is available in the API. 

- `install.sh`: This file installs the necessary libraries to run the python script.
 
- `start.sh`: This file starts the execution of the python script.
 
- `running.sh`: This file checks if the python script is running.
  
- `stop.sh`: This file stops the execution of the python script.