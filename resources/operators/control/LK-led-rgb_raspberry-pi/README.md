# Control Operator: Scripts to control a LK LED RGB

This folder contains operator scripts to control a [Linkerkit LED RGB](http://www.linkerkit.de/index.php?title=LK-LED-RGB) actuator.

## Hardware Setup 

- a Raspberry Pi.  
- (optional) a [LK base board](http://www.linkerkit.de/index.php?title=LK-Base-RB_2).  
- a [LK LED RGB actuator](http://www.linkerkit.de/images/2/24/LK-LED-RGB_17-05-2017.pdf) or any WS281X compatible LEDs like Neopixels plugged to the `digital pin GPIO12` of the Raspberry Pi or `D12` of the LK base board.
- since this uses PWM, which is by default also used for audio on the PI we first need to disable PWM audio like so: https://pypi.org/project/rpi-ws281x/

## Operator files 

- `LK-led-rgb_raspberry-pi.py`: This python script contains a MQTT client, which subscribes to a configured topic on the MBP.
 
- `install.sh`: This file installs the necessary libraries to run the python script.
 
- `start.sh`: This file starts the execution of the python script.
 
- `running.sh`: This file checks if the python script is running.
  
- `stop.sh`: This file stops the execution of the python script.