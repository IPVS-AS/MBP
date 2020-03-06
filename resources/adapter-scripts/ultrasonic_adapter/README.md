# Extraction Operator: Scripts to extract data from a Ultrasonic sensor plugged to a ESP8266-12e 

This folder contains operator scripts for reading values from a ultrasonic sensor plugged to a [ESP8266 microcontroller](https://www.jacobsparts.com/items/ESP8266-DEVBOARD). The ESP8266 receive by HTTP the MBP broker address and topic. Once the microcontroller is configured with the correct address it sends the Ultrasonic sensor values to the broker topic via MQTT. 

## Hardware Setup

The hardware setup for this operator corresponds to:
- a WLAN router or a device configured as WiFi hotspot, to which the following hardware and the MBP computer are connected to.
- a ESP8266-12e device running the `esp_ultrasonic.ino` project.
- a Ultrasonic sensor module. The one used is HC-SR04.

### ESP8266-12e device Setup

To configure the ESP device, the [Arduino IDE](https://www.arduino.cc/en/main/software) is used.

First you need to open the IDE and install some libraries with the Library Manager.

* PubSubClient by Nick O'Leary (version 2.7.0 is used)
* ArduinoJson by Benoit Blanchon (version 6.11.3 is used)

The second step is configure the IDE to use the ESP.
1. Connect the NodeMCU with your PC or laptop with a micro USB cable.
2. Open the IDE, then open **preference** from the **file** menu and copy this link to **additional board manager URLs**: http://arduino.esp8266.com/stable/package_esp8266com_index.json and click **OK**.
3. Open **board manager** from tools -> board -> board manager and search from **nodemcu**. Then select the latest version from the dropdown menu and click install and restart the arduino IDE.
4. If everything is installed properly then you should be able to see the newly installed boards under tools -> board menu. Select **NodeMCU 1.0 (ESP-12E Module)**.
5. Clicking in **Tools** the configuration should be as shown in the screenshot.

[Config](config.png)

More information can be found [here](https://create.arduino.cc/projecthub/najad/using-arduino-ide-to-program-nodemcu-33e899).


### Ultrasonic sensor setup

With the ESP properly configured, now it is possible to plug the sensor to the device.

The project is based on [this](https://www.instructables.com/id/Distance-Measurement-Using-HC-SR04-Via-NodeMCU/) site. So, to build the protoboard with the components and cables follow the instructions presented there. **The project built in the lab has some minor modifications**

Now import the `esp_ultrasonic.ino` project, update the below properties in the code and build the project. 

Edit the following constants to work in your environment:

**ssid**: the SSID of the WiFi network, which the ESP8266-12e device will connect to.  
**password**: WiFi network password.  

Finally, when the application run it will connect to the given WiFi and wait for a HTTP push request with the broker address, topic, etc. (the *adapter_distance.py* script deals with it). Once it receives the broker configuration, it starts to send the distance value measured by the ultrasonic sensor to the broker. 

## Parameters

The following parameters need to be provided on deployment:

 - `"esp_ip" (text)`(required): address of the esp device running the project. The script needs to know the IP to send the HTTP push request.

## Operator files 

The following files are provided in this folder:
 
- `esp_ultrasonic.ino`: This folder contains the `esp_ultrasonic.ino` project, which must be imported into the Arduino IDE and build to the ESP device.
 
- `adapter_distance.py`: This file is resposible for receive the ESP device IP as parameter (from the MBP plataform) and send to this IP a HTTP push request with the MBP broker address.

Example of a HTTP push request:
```
{ 
    "ip"  : "129.69.209.118", 
    "topic" : "sensor/5ddcf4726d7b9c0dc6b5b5ed",
    "component"  : "sensor", 
    "componentId" : "5ddcf4726d7b9c0dc6b5b5ed",
    "status" : "0"
}
```

- `install.sh`: This file installs the necessary libraries to run the python script.
 
- `start.sh`: This file starts the execution of the python script.
 
- `running.sh`: This file checks if the python script is running.
  
- `stop.sh`: This file stops the execution of the python script.
