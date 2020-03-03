# Extraction Operator: Scripts to extract sensor data from a Bosch XDK

This folder contains operator scripts for reading values from a set of sensors of a [Bosch XDK device](https://xdk.bosch-connectivity.com/) via MQTT protocol. 

## Hardware Setup

The hardware setup for this adapter corresponds to:
- a WLAN router or a device configured as WiFi hotspot, to which the following hardware and the MBP computer are connected to.
- a XDK Bosch device running the `XDK-device-extractor` project.
- a Raspberry Pi 3B (Stretch or Buster OS) or a Linux-based computer running a MQTT broker. This device acts as a gateway for the XDK device, since it is not possible to deploy software remotely on XDK devices.

### XDK Bosch device Setup

To configure the XDK device, the [XDK Workbench 3.6.0](https://developer.bosch.com/web/xdk/downloads) is used.

1. import the `XDK-device-extractor` project, update the above properties in `source/AppController.h` and build the project. To import/export a project, check [here](https://developer.bosch.com/web/xdk/importing-a-project).

Edit the following constants to work in your environment:
**WLAN_SSID**: the SSID of the WiFi network, which the XDK device will connect to.  
**WLAN_PSK**: WiFi network password.  
**APP_MQTT_BROKER_HOST_URL**: the MQTT broker host address URL. For the hardware setup above, this corresponds to the Raspberry Pi.  
**APP_MQTT_CLIENT_ID**: the device name, such as `XDK1`.  
**APP_MQTT_TOPIC**: the topic, to which XDK sensor data will be published, such as `XDK/XDK1`.   

2. plug in the XDK device to your computer (USB cable) and connected it to XDK Workbench.
3. flash the project to the XDK device.

## Parameters

The following parameters need to be provided on deployment:

 - `"sensor" (text)`(required): name of sensor which want to read the values, e.g., "humidity", "temperature", or "light"

 - `"id" (text)`(required): XDK's MQTT Client ID (E.g. XDK1, XDK2, XDK3...)

## Operator files 

The following files are provided in this folder:
 
- `XDK-device-extractor`: This file contains the `XDK-device-extractor` project, which must be imported into the XDK Workbench.
 
- `bosch-xdk_mqtt-gateway.py`: This file contains a MQTT client, that receives sensor data from XDK devices, and furthermore, transform and forward these data to a configured topic on the MBP message broker. This operator file should be deployed on a device running a MQTT broker, since it subscribes to `localhost` to receive a json string containing all the sensor values of the XDK device. 

Example of a received JSON string:
```
{ 
    "accelerometer"  : { "x" : "-20",  "y" : "-14", "z" : "1014"}, 
    "gyro" : { "x" : "122",  "y" : "61", "z" : "122"},
    "magnetometer"  : { "x": "22",  "y" : "-17", "z" : "-58", "r" : "6739"}, 
    "humidity" : "31",
    "pressure" : "96725",
    "light" : "25920",
    "temperature" : "25.551000"
}
```

- `install.sh`: This file installs the necessary libraries to run the python script.
 
- `start.sh`: This file starts the execution of the python script.
 
- `running.sh`: This file checks if the python script is running.
  
- `stop.sh`: This file stops the execution of the python script.
