## SO and Compiler Versions

* `Java(TM) SE Runtime Environment (build 1.8.0_91-b14)`  (server machine)
* `Python 3.5.1 :: Anaconda 4.0.0 (64-bit)` (server machine)
* `Raspbian version here` (Raspberry Pi 3) (client machine)
* `RPi python version here` (client machine)

## Repository Structure

* [Java Projects](java-projects) (contains both web and the old base project that implemented the services)

* [Python Scripts](python-scripts) (contains all python services and sensor scripts)

* [RSA Key](rsa-key) (key that should be installed in each RPi in order to use SSH access)

* [Diagram](diagram) (domain diagram for the java project)

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
