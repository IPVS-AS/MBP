# Control Operator: Scripts to control a Wide Angle Fish-Eye Camera

This folder contains operator scripts to control a [Wide Angle Fish-Eye Camera](https://github.com/IPVS-AS/MBP/tree/master/resources/operators) actuator.

## Hardware Setup 

- a Raspberry Pi.  
- a [Raspberry Pi Camera Module Wide Angle Fish-Eye Camera](https://github.com/IPVS-AS/MBP/tree/master/resources/operators) plugged to the `Camera Module port` of the Raspberry Pi.

## Camera Setup

- first the camera needs to be plugged to the `Camera Module port` of the Raspberry Pi
- enable Camera on the Interfaces on the Raspberry Pi Configuration tool and reboot.

More details can be found here: [Getting Started with PiCamera](https://projects.raspberrypi.org/en/projects/getting-started-with-picamera)

## Operator files 

- `adapter_camera.py`: This python script contains a MQTT client, which subscribes to a configured topic on the MBP. When a message is received on the subscribed topic it triggers the camera action and then a picture is taken, encoded and send to the configured endpoint (default endpoint is the MBP broker). When a picture is taken, it is saved on the deployed folder on the Raspberry Pi containing these files (adapter_camera.py and *.sh). **To be fixed**: The Consumer side (that shows the picture on the LCD display) is not included in this code, then the proper script needs to be running on the Raspberry Pi that the LCD display is plugged and configured). 

- `install.sh`: This file installs the necessary libraries to run the python script.
 
- `start.sh`: This file starts the execution of the python script.
 
- `running.sh`: This file checks if the python script is running.
  
- `stop.sh`: This file stops the execution of the python script.