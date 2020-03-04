# Control operator: Scripts to control a LCD display

This folder contains operator scripts to control a `Joy-it LCD display` connected to a Raspberry Pi. Furthemore, the used hardware module has four buttons, which can be pressed to generate audio messages. 

# Hardware setup

The hardware setup for this operator corresponds to:

- a Raspberry Pi.  
- a [Joy-it RB-LCD-16x2 Display-Module 5.6cm (2.22") 16 x 2 Pixel, 4 buttons](https://www.joy-it.net/de/products/RB-LCD-16x2). This display is ready for use, however, the contrast may need to be adjusted manually in order to see any output on the display. To adjust the contrast, turn the adjusting screw (small blue box).  
- a loudspeaker device connected to the audio output of the Raspberry Pi.  
 
 ## Operator files 

- `joyit-lcd-display_raspberry-pi.py`: This python script contains a MQTT client, which subscribes to a configured topic on the MBP.
 
- `install.sh`: This file installs the necessary libraries to run the python script.
 
- `start.sh`: This file starts the execution of the python script.
 
- `running.sh`: This file checks if the python script is running.
  
- `stop.sh`: This file stops the execution of the python script.