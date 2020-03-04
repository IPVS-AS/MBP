# Control operator: Scripts to control a loudspeaker

This folder contains operator scripts to control a loudspeaker of a computer. This operator works on Debian-based operating systems (Raspbian, Ubuntu) and is based on [espeak](http://espeak.sourceforge.net), a text-to-speech (TTS) engine.

## Hardware Setup 

- a computer with a Debian-based OS or a Raspberry Pi. 
- (optional) a loudspeaker device connected to the audio output of the computer or the Raspberry Pi.

## Operator files 

- `loudspeaker_computer.py`: This python script contains a MQTT client, which subscribes to a configured topic on the MBP and uses the tts engine to generate audio outputs.
 
- `install.sh`: This file installs the necessary libraries to run the python script.
 
- `start.sh`: This file starts the execution of the python script.
 
- `running.sh`: This file checks if the python script is running.
  
- `stop.sh`: This file stops the execution of the python script.