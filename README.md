##SO and Compiler Versions

* `Java(TM) SE Runtime Environment (build 1.8.0_91-b14)`  (server machine)
* `Python 3.5.1 :: Anaconda 4.0.0 (64-bit)` (server machine)
* `Raspbian version here` (Raspberry Pi 3) (client machine)
* `RPi python version here` (client machine)

##Libraries
    
On the Java Maven project, you can check pom.xml for the libraries and respective versions.
* [Paho MQTT 3.1.1](https://eclipse.org/paho/clients/java/)
Used for communication between local server and devices (Raspberries and so on).
Download used version [here](https://repo.eclipse.org/content/repositories/paho-releases/org/eclipse/paho/org.eclipse.paho.client.mqttv3/1.0.2/).

* [Scapy](http://www.secdev.org/projects/scapy/)
Used on arping.py - runs only on server machine. On *Windows* the following is needed:
[WinPCap](https://www.winpcap.org/).

## Server and services

* [mongoDB server](https://www.mongodb.com/download-center?jmp=nav#community) on server machine
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

* [Mosquitto MQTT Broker](https://mosquitto.org/download/) on server machine
Download and install the server. It should start on its own.
Version: `mosquitto version 1.4.8 (build date 14/02/2016 15:33:31.09)`


## Packets and others

* Upstart packet on client machine
Run `sudo apt-get install upstart`, reboot after installation. This is used to set up services on the system.

* [WinPCap](https://www.winpcap.org/) on server machine (Windows only)
Compliments Scapy library.



    
