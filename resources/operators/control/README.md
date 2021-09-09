# MBP Control Operators

This folder contains operator scripts to bind actuators to the MBP. The communication between the IoT device executing this category of operator and the MBP is done through the MQTT protocol. 

MQTT topics for *actuators* follows the structure 'actuator/$actuator_id'. Since the *ids* are generated on the registration of actuators to the MBP, the operator scripts are parameterized, so that the *ids* are passed to the operator scripts during the deployment of these operators onto the IoT devices.

The message structure that the MBP understands is a json-formatted string containing the following elements:
 - "component" : "ACTUATOR"
 - "id" : $actuator_id
 - "value" : $actuator_value

The following shows a CLI example using the paho MQTT client: 

``    
    $ mosquitto_pub.exe -t actuator/5ddfd372ddc3d67cf740d6a5 
      -m '{"component": "ACTUATOR", "id": "5ddfd372ddc3d67cf740d6a5", "value": 1}'
``

The name convention for subfolders is `<actuator-type>_<IoT-device-type>`.

## Content

- [actuator_stub](actuator_stub): operator scripts to simulate an actuator.
- [camera_raspberry-pi](camera_raspberry-pi): operator scripts to control a wide angle fish-eye camera.
- [joyit-lcd-display_raspberry-pi](joyit-lcd-display_raspberry-pi): operator scripts to control a joy-it lcd display actuator. 
- [LK-buzzer_raspberry-pi](LK-buzzer_raspberry-pi): operator scripts to trigger a LK buzzer actuator.
- [LK-led-rgb_raspberry-pi](LK-led-rgb_raspberry-pi): operator scripts to control a LK LED RGB actuator.
- [loudspeaker_computer](loudspeaker_computer): operator scripts to control a loudspeaker actuator.
- [relay_raspberry-pi](relay_raspberry-pi): operator scripts to control a relay.
- [switch_raspberry-pi](switch_raspberry-pi): operator scripts to control a radio frequency (RF), Arduino-based switch.

## Note on data model definition

As many here listed actuators do not send any data to the MBP they can use an arbitrary data model. Therefore, for these actuators
no data model json definition is provided in their folders.