# Control Operator: Scripts to control a wide angle fish-eye camera

This folder contains operator scripts to control the [Sainsmart wide angle fish-eye camera](https://www.sainsmart.com/products/noir-wide-angle-fov160-5-megapixel-camera-module) actuator.

## Hardware Setup 

- a Raspberry Pi.  
- a [Sainsmart wide angle fish-eye camera](https://www.sainsmart.com/products/noir-wide-angle-fov160-5-megapixel-camera-module) plugged to the `camera module port` of the Raspberry Pi.

### Camera Setup

- plug the camera to the [camera module port](https://projects.raspberrypi.org/en/projects/getting-started-with-picamera/1) of the Raspberry Pi.
- enable `Camera interface` on the Raspberry Pi configuration tool `raspi-config` and reboot the Raspberry Pi.

For more details to the camera configuration: [Getting Started with PiCamera](https://projects.raspberrypi.org/en/projects/getting-started-with-picamera)

## Operator files 

- `camera_raspberry-pi.py`: This python script contains a MQTT client, which subscribes to a configured topic on the MBP. When a message is received on the subscribed topic it triggers the camera action and then a picture is taken, encoded and sent to the configured endpoint. The default endpoint is the MBP broker endpoint. 
When a picture is taken, it is saved on the deployed folder on the Raspberry Pi containing these operator files (camera_raspberry-pi.py, \*.sh). 
**To be fixed**: An actuator to show the picture on the LCD display is not yet included in this code, then the proper control operator script needs to be running on the Raspberry Pi, to which the LCD display is plugged and configured. 

- `camera.py`: TBD  
- `mqttClient.py`: TBD  
- `send.py`: TBD  
- `install.sh`: This file installs the necessary libraries to run the python script.
 
- `start.sh`: This file starts the execution of the python script.
 
- `running.sh`: This file checks if the python script is running.
  
- `stop.sh`: This file stops the execution of the python script.

## Data model support

Since the here provided scripts send a MQTT message for each camera image to a "image" MQTT topic (in a not normalized message format) it can't be handled like usual sensor data (must be considered separately by the MBP implementation). 
Thefore no data model support is provided 