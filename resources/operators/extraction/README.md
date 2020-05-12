# MBP Extraction Operators

This folder contains operator scripts to bind sensors to the MBP. The communication between the IoT device executing this category of operator and the MBP is done through the MQTT protocol. 

MQTT topics for *sensors* follows the structure 'sensor/$sensor_id'.
Since the *ids* are generated on the registration of sensors to the MBP, the operator scripts are parameterized, so that the *ids* are passed to the operator scripts during the deployment of these operators onto the IoT devices.

The expected message structure by the MBP is a json-formatted string containing the following elements:
 - "component" : "SENSOR"
 - "id" : $sensor_id
 - "value" : $sensor_value

The following shows a CLI example using the paho MQTT client: 
 
``
    $ mosquitto_pub.exe -t sensor/596cafaa6c0ccd5d29da0e90 
      -m '{"component": "SENSOR", 
           "id": "596cafaa6c0ccd5d29da0e90", 
	          "value": 20}'
``

The name convention for subfolders is `<sensor-type>_<IoT_device-type>`.

## Content

- [bosch-xdk_mqtt-gateway](bosch-xdk_mqtt-gateway): operator scripts to extract sensor data from Bosch XDK devices.
- [LK-light_raspberry-pi](LK-light_raspberry-pi): operator scripts to extract sensor data from a LK light sensor.
- [LK-temperature_raspberry-pi](LK-temperature_raspberry-pi): operator scripts to extract sensor data from a LK temperature sensor. 
- [temperature_stub](temperature_stub): operator scripts to simulate the extraction of temperature sensor data. 
- [temperature_stub_param](temperature_stub_param): operator scripts to simulate the extraction of temperature sensor data, which take input parameters upon their deployment. 
