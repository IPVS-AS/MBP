# MBP Extraction Operators

This folder contains operator scripts to connect sensors to the MBP. The communication between the IoT device executing operators and the MBP is done through the MQTT protocol. 

MQTT topics for `sensors` follows the structure `sensor/$sensor_id`. 

Since the sensor`id` is generated upon registration by the MBP, the operator scripts are parameterized, so that the sensor `id` is passed to the operator scripts during the deployment on the IoT devices.

The expected message structure by the MBP is a json-formatted string with the following attributes:
```json
{
	"component": "SENSOR", 
	"id": "596cafaa6c0ccd5d29da0e90", 
	"value": {"temperature:" 20}
}
```
The `value` key can have an arbitrary JSON object as value. However the structure of this JSON object must be registered to the MBP by creating a data model entity.

The following shows a command line example using the paho MQTT client to send sensor data to the MBP: 

```bash
$ mosquitto_pub.exe -t 'sensor/596cafaa6c0ccd5d29da0e90' -m '{"component":"SENSOR","id":"596cafaa6c0ccd5d29da0e90","value": {"temperature:" 20}}'
```



## Content

:warning: The name convention for subfolders is `<sensor-type>_<IoT_device-type>`.

- [Adafruit_DHT-temperature_raspberry-pi](Adafruit_DHT-temperature_raspberry-pi) :thermometer: :droplet: : scripts to extract sensor data from a DHT sensor. 
- [bosch-xdk_mqtt-gateway](bosch-xdk_mqtt-gateway) :thermometer: :partly_sunny: :droplet: : operator scripts to extract sensor data from Bosch XDK devices.
- [complex_iot_data_stub](complex_iot_data_stub) :sensor:: operator scripts using a more complex data model demonstrating the complex IoT data capabilities of the MBP.
- [LK-light_raspberry-pi](LK-light_raspberry-pi) :partly_sunny:: operator scripts to extract sensor data from a LK light sensor.
- [LK-temperature_raspberry-pi](LK-temperature_raspberry-pi) :thermometer:: operator scripts to extract sensor data from a LK temperature sensor.
- [LK-sound_raspberry-pi](LK-sound_raspberry-pi) :microphone:: operator scripts to extract sensor data from a LK sound sensor.
-  [miflora-plant_raspberry-pi](miflora-plant_raspberry-pi):seedling: :thermometer: :partly_sunny: :droplet: : scripts to extract several sensor data from a MiFlora plant sensor.
- [temperature_stub](temperature_stub) :thermometer: : operator scripts to simulate the extraction of temperature sensor data. 
- [temperature_stub_param](temperature_stub_param) :thermometer:: operator scripts to simulate the extraction of temperature sensor data, which take input parameters upon their deployment. 

Further folders:

* [simulators](simulators): simulators of different sensors, which can generate sensor data for different events combined with anomalies. These simulators can be used for testing rule-based IoT applications to detect failures and take actions against them.
