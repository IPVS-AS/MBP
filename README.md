# Multi-purpose Binding and Provisioning Platform (MBP)
This platform enables means for (i) automated binding of IoT devices in order to access their sensors and actuators, and (ii) automated software provisioning.

## Installation and Configuration
see [User Manual](user_manual.md)

## REST API
A REST API for the management of devices, sensors and actuators is provided.

### Devices

#### creating a new device:


POST /api/devices/ HTTP/1.1  
Content-Type: application/json

```javascript
{
  "name": "Raspberry Pi",
  "macAddress": "123456789067",
  "ipAddress": "192.168.0.34",
  "formattedMacAddress": "12-34-56-78-90-67"
}
```

HTTP/1.1 201 Created

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
  "ipAddress": 192.168.0.34,
  "name": "Raspberry Pi",
  "id": "596c7a7d4f0c58688e5aa6b1",
  "date": null,
}
```

#### updating single device (e.g., for id 596c7a7d4f0c58688e5aa6b1):

PUT /api/devices/596c7a7d4f0c58688e5aa6b1 HTTP/1.1

```javascript
{
  "name": "Raspberry Pi",
  "macAddress": "127556789067",
  "ipAddress": "192.168.0.75",
  "formattedMacAddress": "12-75-56-78-90-67"
}
```

HTTP/1.1 204 No Content

#### delete single device (e.g., for id 596c7a7d4f0c58688e5aa6b1):

DELETE /api/devices/596c7a7d4f0c58688e5aa6b1 HTTP/1.1

HTTP/1.1 204 No Content

### Adapter Types
An adapter is the required software (e.g., python script) to bind sensors and actuators to the MBP.

#### creating a new adapter type:
POST /api/types/ HTTP/1.1
Content-Type: application/json

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

### Sensors

#### creating a new sensor with type id 596c7c344f0c58688e5aa6b3 and device id 596c7a7d4f0c58688e5aa6b1:
POST /api/sensors/ HTTP/1.1
Content-Type: application/json

```javascript
{
  "name": "Temperature Sensor",
  "type": "<URL>/api/types/596c7c344f0c58688e5aa6b3",
  "device": "<URL>/api/devices/596c7a7d4f0c58688e5aa6b1",
}
```

HTTP/1.1 201 Created

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

### Actuators

see REST calls for sensors, replace /sensors with /actuators

## Repository Structure

* [Java Projects](java-projects) (contains both web and the old base project that implemented the services)

* [Python Scripts](python-scripts) (contains all python services and sensor scripts)

* [RSA Key](rsa-key) (key that should be installed in each RPi in order to use SSH access)

* [Diagram](diagram) (domain diagram for the java project)


## SO and Compiler Versions

* `Java(TM) SE Runtime Environment (build 1.8.0_91-b14)`  (server machine)
* `Python 3.5.1 :: Anaconda 4.0.0 (64-bit)` (server machine)
* `Raspbian version here` (Raspberry Pi 3) (client machine)
* `RPi python version here` (client machine)


## Servers and 3rd-party softwares

### [mongoDB server](https://www.mongodb.com/download-center?jmp=nav#community) on server machine

Install the server. To start the server, run ./Server/**version**/bin/mongod.exe inside the mongoDB installation directory.
Version: 
``` 
  db version v3.2.7
  git version: 4249c1d2b5999ebbf1fdf3bc0e0e3b3ff5c0aaf2
  OpenSSL version: OpenSSL 1.0.1p-fips 9 Jul 2015
  allocator: tcmalloc
  modules: none
  build environment:
  distmod: 2008plus-ssl
  distarch: x86_64
  target_arch: x86_64
```


### [Mosquitto MQTT Broker](https://mosquitto.org/download/) on server machine

Download and install the server. It should start on its own.

Version: `mosquitto version 1.4.8 (build date 14/02/2016 15:33:31.09)`


## Packets and others

### Upstart packet on client machine

Run `sudo apt-get install upstart`, reboot after installation.

This is used to set up services on the system.

To set a service (%name% stands for any name):

* create a ```%name%.conf``` file in ```/etc/init```
* run ```sudo initctl reload-configuration```
* run ```sudo service %name% start```


### [WinPCap](https://www.winpcap.org/) on server machine (Windows only - not needed on Linux dists)

Compliments Scapy library.


### [SSH RSA Key](https://help.ubuntu.com/community/SSH/OpenSSH/Keys)

On the client machine, run:
```
mkdir ~/.ssh
chmod 700 ~/.ssh
```
and then copy the public key found in [rsa](./rsa) to ~./ssh.
