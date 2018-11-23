# Multi-purpose Binding and Provisioning Platform (MBP)
This platform enables means for (i) automated binding of IoT devices in order to access their sensors and actuators, and (ii) automated software provisioning.

![MBP-UI](https://github.com/ana-silva/MBP/blob/master/MBP.PNG)  

How to install the MBP and use its API is explained in detail in the following:  

**[1 Installation and Configuration](#1-installation-and-configuration)**  
**[2 MBP REST API](#2-mbp-rest-api)**  
**[2.1 Devices](#21-devices)**  
**[2.2 Adapter Types](#22-adapter-types)**  
**[2.3 Sensors](#23-sensors)**  
**[2.1 Actuators](#24-actuators)**  

## 1 Installation and Configuration

The following sofware components are used in order to set up the MBP:  
- [Mosquitto MQTT Broker](https://mosquitto.org/download/)
- [mongoDB server](https://www.mongodb.com/download-center?jmp=nav#community)
- Java8
- [Tomcat8](https://tomcat.apache.org/download-80.cgi)
- Maven
- Bootstrap template: [https://startbootstrap.com/template-overviews/sb-admin-2/](https://startbootstrap.com/template-overviews/sb-admin-2/)

### 1.1 Configuration
Before starting the installation, please set the MQTT broker IP address in the configuration file [config.properties](src/main/resources/config.properties). In order to allow the MBP to access registered devices, the devices must be configured to be accessed by SSH using a [RSA Key](resources/rsa-key).

### 1.2 Installation on Linux 
Please run the [installation script](install.sh), which automatically install the sofware components listed above. Once the installation is completed, the MBP will be available on the URL *http://MBP-Host:8080/MBP*.  

### 1.3 Installation on Windows
Please execute the following steps:  
- Install and start [Mosquitto MQTT Broker](https://mosquitto.org/download/), [mongoDB server](https://www.mongodb.com/download-center?jmp=nav#community), and [Tomcat8](https://tomcat.apache.org/download-80.cgi)   
- Create the *MBP.war* file by building the provided maven project
    
    $ mvn clean install  
    
- Deploy the MBP application on Tomcat by moving *MBP.war* to the Tomcat webapps folder  

Once the installation is completed, the MBP will be available on the URL *http://MBP-Host:8080/MBP*.  

### 1.4 Using the MBP
The MBP provides two user roles, *expert* and *normal*. As an *expert user*, it is possible to register *Adapter types*, *Devices*, *Sensors* and *Actuators*. As a *normal user* it is possible to register only *Sensors* and *Actuators*.

In order to register a sensor or an actuator, the correponding *Adapter type* and *Device* must be registered before the sensor or actuator.

Once a sensor is registered, pushing sensor data to the MBP, or receiving sensor data, can be done through a MQTT Client. Sensor topics follows the structure 'sensor/$sensor_id' while actuators follows the structure 'actuator/$actuator_id'. The *ids* are generated on the registration step and are shown in the MBP UI.

The message format is a json string containing at least the following elements:
 - "component" : [ "SENSOR", "ACTUATOR" ] <- one of the values, accordingly
 - "id" : $id <- sensor or actuator id
 - "value" : $value

For example,

    
    $ mosquitto_pub.exe -t sensor/596cafaa6c0ccd5d29da0e90 -m '{"component": "SENSOR", "id": "596cafaa6c0ccd5d29da0e90", "value": 20}'
    

## 2 MBP REST API
A REST API for the management of devices, adapter types, sensors and actuators is provided. Furthermore, an interface to trigger the binding of sensors and actuator is as well provided. Finally, sensor and actuator values sent to the MBP can be retrieved.

Make sure that CORS (Cross-Origin Resource Sharing) is allowed for all origins. To do that, add the following filter to Tomcat's 'web.xml':

```xml
<filter>
  <filter-name>CorsFilter</filter-name>
  <filter-class>org.apache.catalina.filters.CorsFilter</filter-class>
</filter>
<filter-mapping>
  <filter-name>CorsFilter</filter-name>
  <url-pattern>/*</url-pattern>
</filter-mapping>
```

### 2.1 Devices

#### creating a new device:


POST /api/devices/ HTTP/1.1  
Content-Type: application/json  
accept: application/json
```javascript
{
  "name": "Raspberry Pi",
  "macAddress": "123456789067",
  "ipAddress": "192.168.0.34",
  "formattedMacAddress": "12-34-56-78-90-67",
  "username": "pi"
}
```

HTTP/1.1 201 Created  
location: http://localhost:8080/MBP/api/devices/5a033094c27074e37bbb198b  
content-type:
application/json;charset=UTF-8
```javascript
{
  "id": "5a033094c27074e37bbb198b",
  "name": "Raspberry Pi",
  "macAddress": "123456789067",
  "ipAddress": "192.168.0.34",
  "username": "pi",
  "date": null,
  ...
}
```
The **id** of the newly created device can be parsed from the response header *location* or from the response body, which is in json format.  

#### retrieving all devices:
GET /api/devices/ HTTP/1.1  

HTTP/1.1 200 OK

```javascript
[
  {
    "macAddress": "123456789067",
    "ipAddress": "192.168.0.34",
    "name": "Raspberry Pi",
    "id": "596c7a7d4f0c58688e5aa6b1",
    "date": null,
  }, ...
]
```

#### retrieving single device (e.g., for id 596c7a7d4f0c58688e5aa6b1):

GET /api/devices/596c7a7d4f0c58688e5aa6b1 HTTP/1.1

HTTP/1.1 200 OK

```javascript
{
  "macAddress": "123456789067",
  "ipAddress": "192.168.0.34",
  "name": "Raspberry Pi",
  "id": "596c7a7d4f0c58688e5aa6b1",
  "date": null,
  "username": "pi",
}
```

#### updating single device (e.g., for id 596c7a7d4f0c58688e5aa6b1):

PUT /api/devices/596c7a7d4f0c58688e5aa6b1 HTTP/1.1

```javascript
{
  "name": "Raspberry Pi",
  "macAddress": "127556789067",
  "ipAddress": "192.168.0.75",
  "formattedMacAddress": "12-75-56-78-90-67",
  "username": "pi"
}
```

HTTP/1.1 204 No Content

#### delete single device (e.g., for id 596c7a7d4f0c58688e5aa6b1):

DELETE /api/devices/596c7a7d4f0c58688e5aa6b1 HTTP/1.1

HTTP/1.1 204 No Content

### 2.2 Adapter Types
An adapter is the required software (e.g., python script) to bind sensors and actuators to the MBP.
Examples to such adapters can be found in [Adapter Scripts](resources/adapter-scripts). Each adapter shall be composed of at least the files *install.sh* and *start.sh*. Furthermore, by including the files *running.sh* and *stop.sh* in the adapter, it allows the MBP to check if the adapter is currently running and to undeploy the adapter.   

#### creating a new adapter type:
POST /api/types/ HTTP/1.1  
Content-Type: application/json  
accept: application/json  
```javascript
{
  "name": "TemperatureSensorAdapter",
  "description": "An adapter for the LK temperature sensor",
  "service": {
    "name": "service-stub.conf",
    "content": "stub conf"
  },
  "routines": [{
    "name": "service-stub.py",
    "content": "service code"
  }]
}
```

HTTP/1.1 201 Created  
location: http://localhost:8080/MBP/api/types/5a0336ba972ca8734022d67c  
content-type: application/json;charset=UTF-8  
```javascript
{
  "id": "5a0336ba972ca8734022d67c",
  "name": "TemperatureSensorAdapter",
  "description": "An adapter for the LK temperature sensor",
  ...
}
```
The **id** of the newly created adapter type can be parsed from the response header *location* or from the response body, which is in json format.  

#### retrieving all adapter types:
GET /api/types/ HTTP/1.1

HTTP/1.1 200 OK

```javascript
[
  {
    "name": "TemperatureSensorAdapter",
    "id": "596c7c344f0c58688e5aa6b3",
    "description": "An adapter for the LK temperature sensor"
  }, ...
]
```

#### retrieving single adapter type (e.g., for id 596c7c344f0c58688e5aa6b3):

GET /api/types/596c7c344f0c58688e5aa6b3 HTTP/1.1

HTTP/1.1 200 OK

```javascript
{
  "name": "TemperatureSensorAdapter",
  "id": "596c7c344f0c58688e5aa6b3",
  "description": "An adapter for the LK temperature sensor"
}
```

#### updating single adapter type (e.g., for id 596c7c344f0c58688e5aa6b3):

PUT /api/types/596c7c344f0c58688e5aa6b3 HTTP/1.1

```javascript
{
  "name": "TemperatureSensorAdapter",
  "description": "An adapter for the LK temperature sensor",
  "service": {
    "name": "service-stub.conf",
    "content": "..."
  },
  "routines": [{
    "name": "service-stub.py",
    "content": "..."
  }]
}
```

HTTP/1.1 204 No Content

#### delete single adapter type (e.g., for id 596c7c344f0c58688e5aa6b3):

DELETE /api/types/596c7c344f0c58688e5aa6b3 HTTP/1.1

HTTP/1.1 204 No Content

### 2.3 Sensors  
To register a sensor, it is necessary to register first:  
 (i) the device to which the sensor is connected to, and  
 (ii) the adapter type, i.e., the required software (e.g., python script) to bind the sensor to the MBP. 
  
#### creating a new sensor:  
The following example uses the previously registered adapter type (id = 596c7c344f0c58688e5aa6b3) and device (id = 596c7a7d4f0c58688e5aa6b1):

POST /api/sensors/ HTTP/1.1  
Content-Type: application/json  
accept: application/json  

```javascript
{
  "name": "Temperature Sensor",
  "type": "<URL>/api/types/596c7c344f0c58688e5aa6b3",
  "device": "<URL>/api/devices/596c7a7d4f0c58688e5aa6b1",
}
```

HTTP/1.1 201 Created  
location: http://localhost:8080/MBP/api/sensors/596c7a974f0c58688e5aa6b2  
content-type: application/json;charset=UTF-8  
```javascript
{
  "id": "596c7a974f0c58688e5aa6b2",
  "name": "test_sensor",
  ...
}
```
The **id** of the newly created sensor can be parsed from the response header *location* or from the response body, which is in json format.  

#### retrieving all sensors:
GET /api/sensors/ HTTP/1.1

HTTP/1.1 200 OK

```javascript
[
  {
    "id": "596c7a974f0c58688e5aa6b2",
    "name": "test_sensor",
    "_embedded": {
      "device": {
        "macAddress": "111111111111",
        "ipAddress": null,
        "name": "Test_Device",
        "id": "596c7a7d4f0c58688e5aa6b1",
        "date": null
      }
    }
  }, ...
]
```

#### retrieving single sensor (e.g., for id 596c7a974f0c58688e5aa6b2):

GET /api/sensors/596c7a974f0c58688e5aa6b2 HTTP/1.1

HTTP/1.1 200 OK

```javascript
{
  "id": "596c7a974f0c58688e5aa6b2",
  "name": "test_sensor",
  "_embedded": {
    "device": {
      "macAddress": "111111111111",
      "ipAddress": null,
      "name": "Test_Device",
      "id": "596c7a7d4f0c58688e5aa6b1",
      "date": null
    }
  }
}
```

#### updating single sensor (e.g., for id 596c7a974f0c58688e5aa6b2):

PUT /api/sensors/596c7a974f0c58688e5aa6b2 HTTP/1.1

```javascript
{
  "name": "Temperature Sensor",
  "type": "<URL>/api/types/596c7c344f0c58688e5aa6b3",
  "device": "<URL>/api/devices/596c7a7d4f0c58688e5aa6b1",
}
```

HTTP/1.1 204 No Content

#### delete single sensor (e.g., for id 596c7a974f0c58688e5aa6b2):

DELETE /api/sensors/596c7a974f0c58688e5aa6b2 HTTP/1.1

HTTP/1.1 204 No Content

#### deploying the adapter for a sensor (e.g., for id 596c7a974f0c58688e5aa6b2):

POST /api/deploy/sensor/596c7a974f0c58688e5aa6b2

HTTP/1.1 201 Created 

#### undeploying adapter of a sensor (e.g., for id 596c7a974f0c58688e5aa6b2):

DELETE /api/deploy/sensor/596c7a974f0c58688e5aa6b2 HTTP/1.1

HTTP/1.1 204 No Content

#### retrieving sensor values (e.g., for id 5b06834c4f0ca35062f25360): 

GET /api/valueLogs/search/findAllByIdref?idref=5b06834c4f0ca35062f25360 HTTP/1.1

HTTP/1.1 200 OK

```javascript
{
  "_embedded" : {
    "valueLogs" : [ {
      "qos" : 0,
      "topic" : "sensor/5b06834c4f0ca35062f25360",
      "message" : "{\"component\": \"SENSOR\", \"id\": \"5b06834c4f0ca35062f25360\", \"value\": \"42\"}",
      "date" : "2018-05-30 10:48:41",
      "idref" : "5b06834c4f0ca35062f25360",
      "component" : "SENSOR",
      "value" : "42",
      "_links" : {
        "self" : {
          "href" : "http://192.168.209.189:8080/MBP/api/valueLogs/5b0e81898315295d360367b0"
        },
        "valueLog" : {
          "href" : "http://192.168.209.189:8080/MBP/api/valueLogs/5b0e81898315295d360367b0"
        },
        "sensorRef" : {
          "href" : "http://192.168.209.189:8080/MBP/api/valueLogs/5b0e81898315295d360367b0/sensorRef"
        },
        "actuatorRef" : {
          "href" : "http://192.168.209.189:8080/MBP/api/valueLogs/5b0e81898315295d360367b0/actuatorRef"
        }
      }
    }, {...}
  }
}
```

To retrieve values so that they are ordered by date, where the first value was the last value received use:
    
    /api/valueLogs/search/findAllByIdref?idref=5b06834c4f0ca35062f25360&sort=date,desc
    
To retrieve only the last value use:
    
    /api/valueLogs/search/findAllByIdref?idref=5b06834c4f0ca35062f25360&page=0&size=1&sort=date,desc
    
### 2.4 Actuators

see REST calls for sensors, replace **/sensors** with **/actuators**

## Haftungsausschluss

Dies ist ein Forschungsprototyp.
Die Haftung für entgangenen Gewinn, Produktionsausfall, Betriebsunterbrechung, entgangene Nutzungen, Verlust von Daten und Informationen, Finanzierungsaufwendungen sowie sonstige Vermögens- und Folgeschäden ist, außer in Fällen von grober Fahrlässigkeit, Vorsatz und Personenschäden, ausgeschlossen.

## Disclaimer of Warranty

Unless required by applicable law or agreed to in writing, Licensor provides the Work (and each Contributor provides its Contributions) on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied, including, without limitation, any warranties or conditions of TITLE, NON-INFRINGEMENT, MERCHANTABILITY, or FITNESS FOR A PARTICULAR PURPOSE.
You are solely responsible for determining the appropriateness of using or redistributing the Work and assume any risks associated with Your exercise of permissions under this License.
