# MBP Operators Repository

This folder contains exemplary operator scripts that can be used to bind sensors and actuators to the MBP. The communication between an IoT device, to which a sensor is connected, and the MBP is done through the MQTT protocol. 

The operator scripts in this folder use a MQTT client to push sensor data to the MBP, or receive control commands. 

MQTT topics for *sensors* follows the structure 'sensor/$sensor_id' while actuators follows the structure 'actuator/$actuator_id'. Since the *ids* are generated on the registration step of sensors/actuators, the operator scripts are parameterized, so that the *ids* are passed to the operator scripts during the deployment of these operators onto the IoT devices.

The message structure that the MBP understands is a json-formatted string containing the following elements:
 - "component" : [ "SENSOR", "ACTUATOR" ] <- one of the values, accordingly
 - "id" : $id <- sensor or actuator id
 - "value" : $value

For example,
    
    $ mosquitto_pub.exe -t sensor/596cafaa6c0ccd5d29da0e90 
      -m '{"component": "SENSOR", 
           "id": "596cafaa6c0ccd5d29da0e90", 
	        "value": 20}'
	

Furthermore, each set of operator scripts shall be composed of at least the files *install.sh* and *start.sh*. By including  additionally the files *running.sh* and *stop.sh* in the operator, it enables the MBP to call these scripts to check if the operator is currently running and to stop it.
