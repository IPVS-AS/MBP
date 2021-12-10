# Extraction Operator: Scripts to extract Adafruit DHT temperature sensor data

This folder contains operator scripts to extract sensor data from a LKAdafruit DHT temperature sensor. 

## Hardware Setup 

 - a Raspberry Pi
 - a Adafruit DHT sensor (https://learn.adafruit.com/dht-humidity-sensing-on-raspberry-pi-with-gdocs-logging/python-setup) 

## Operator files 

 - `Adafruit_DHT-temperature_raspberry-pi.py`: This python script contains a MQTT client, which publishes sensor data to a configured topic on the MBP.
 
 - `install.sh`: This file installs the necessary libraries to run the python script.
 
 - `start.sh`: This file starts the execution of the python script.
 
 - `running.sh`: This file checks if the python script is running.
  
 - `stop.sh`: This file stops the execution of the python script.
 
 - `dataModel.json`: This file contains a data model definition which can be used for the creation of a respective operator entity in the MBP.