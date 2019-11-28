# MBP Control Operators

This folder contains operator scripts to bind actuators to the MBP. The communication between the IoT device executing this category of operator and the MBP is done through the MQTT protocol. 

MQTT topics for *actuators* follows the structure 'actuator/$actuator_id'. Since the *ids* are generated on the registration of actuators to the MBP, the operator scripts are parameterized, so that the *ids* are passed to the operator scripts during the deployment of these operators onto the IoT devices.

The message structure that the MBP understands is a json-formatted string containing the following elements:
 - "component" : "ACTUATOR"
 - "id" : $actuator_id
 - "value" : $actuator_value

The following shows a CLI example using the paho MQTT client: 
    
    $ mosquitto_pub.exe -t actuator/596cafaa6c0ccd5d29da0e90 
      -m '{"component": "ACTUATOR", 
           "id": "596cafaa6c0ccd5d29da0e90", 
           "value": 20}'

The name convention for subfolders is <actuator-type>_<IoT-device-type>.

## Content

- [switch_raspberry-pi](switch_raspberry-pi): operator scripts to control a radio frequency (RF) and Arduino-based switch.