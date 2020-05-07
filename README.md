# Multi-purpose Binding and Provisioning Platform (MBP)

This project contains the **Multi-purpose Binding and Provisioning Platform (MBP)**, an IoT platform developed for easy binding, provisioning, and management of IoT environments. 
Furthermore, the MBP enables the simple realization of IoT applications, such as heating, ventilation, air conditioning (HVAC) systems, by allowing users to create rules for the IoT environment, in a straightforward and event-condition-action fashion. 
The efficient and timely data processing of IoT environments are assured through underlying complex event processing technologies.
An Android-based smartphone application that connects to the MBP is provided as the GitHub project [MBP2Go](https://github.com/IPVS-AS/MBP2Go).

![MBP Home](resources/gifs/user-registration.gif)
MBP UI (based on [Bootstrap templates](https://startbootstrap.com/template-overviews/sb-admin-2/))

How to install and use the MBP is explained in the following.

**[1 Installation](#1-installation)**  
**[2 Quick Start](#2-quick-start)**  
**[3 MBP REST API](#3-mbp-rest-api)**  

## 1 Installation

The following software components are used in order to set up the MBP: [Mosquitto MQTT Broker](https://mosquitto.org/download/), [mongoDB server](https://www.mongodb.com/download-center?jmp=nav#community), [InfluxDB](https://portal.influxdata.com/downloads/), Java8, [Tomcat8](https://tomcat.apache.org/download-80.cgi), and Maven.

### 1.1 Installation on Linux 
Please run the [installation script](install.sh), which automatically installs the aforementioned software components. Once the installation is completed, the MBP will be available on the URL `http://[MBP-Host]:8080/MBP`.  

### 1.2 Installation on Windows
Please execute the following steps:  
- Install and start [Mosquitto MQTT Broker](https://mosquitto.org/download/), [mongoDB server](https://www.mongodb.com/download-center?jmp=nav#community), [InfluxDB](https://portal.influxdata.com/downloads/) and [Tomcat8](https://tomcat.apache.org/download-80.cgi)   
- Create the `MBP.war` file by building the provided maven project
    
    `$ mvn clean install`  
    
- Deploy the MBP application on Tomcat by moving the `MBP.war` to the Tomcat `webapps` folder  

Once the installation is completed, the MBP will be available on the URL `http://[MBP-Host]:8080/MBP`.

### 1.3 Installation on Mac Os
Please execute the following steps:
- Install [Homebrew](https://brew.sh/index_de),
- Install [Mosquitto MQTT Broker](https://mosquitto.org/download/), [mongoDB server](https://www.mongodb.com/download-center?jmp=nav#community), [InfluxDB](https://portal.influxdata.com/downloads/), [Tomcat8](https://tomcat.apache.org/download-80.cgi) and [Maven](https://maven.apache.org/).

    `$ brew install mosquitto`  
    `$ brew install mongodb-community`  
    `$ brew install influxdb`  
    `$ brew install tomcat@8`  
    `$ brew install maven`  

- Start Mosquitto, MongoDB, InfluxDB and Tomcat8.

    `$ brew services start mosquitto`  
    `$ brew services start mongodb-community`  
    `$ brew services start influxdb`  
    `$ brew services start tomcat@8`  
    
    You can check if all of them have started correctly with:
   
   `$ brew services list`  
   
- Create the `MBP.war` file by building the provided maven project
    
    `$ mvn clean install`  
    
- Deploy the MBP application on Tomcat by moving the `MBP-0.1.war` to the Tomcat `webapps` folder in the path:
usr/local/Cellar/tomcat/9.0.10/libexec .
- Restart Tomcat:  
`$ brew services restart tomcat` 

Once the installation is completed, the MBP will be available on the URL `http://[MBP-Host]:8080/MBP-0.1`.

### 1.4 Installation using Docker
To install the MBP as a docker container, please refer to the GitHub project [MBP-Docker](https://github.com/IPVS-AS/MBP-Docker).
This project includes a ready-to-use setup of the MBP application and its dependencies.

### Tip: Cross-Origin Resource Sharing (CORS)

Make sure that [Cross-Origin Resource Sharing (CORS)](https://developer.mozilla.org/en-US/docs/Web/HTTP/CORS) is allowed for all origins. To do that, add the following filter to Tomcat's 'web.xml':

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

### Tip: Improving startup time
After a reboot of the hosting system, in order to decrease the restart time that Tomcat takes to make the MBP application available again, it is helpful to adjust the 'java.security' file of the JRE as suggested [in this post](https://stackoverflow.com/a/26432537). Otherwise it may take up to 30 minutes until the MBP can be accessed again. If you are using Linux, the installation script 'install.sh' takes care of this automatically.

## 2 Quick Start

To start using the MBP, please click [here](https://github.com/IPVS-AS/MBP/wiki/Quick-Start) to see our Quick Start.

## 3 MBP REST API

A REST API for the registration and management of components in an IoT environment is provided. Furthermore, the deployment of software components onto IoT devices, e.g., operators that extract and send sensor values to the MBP, can be as well realized through the MBP API. 

Click [here](https://github.com/IPVS-AS/MBP/wiki/API-Reference) to see the API Reference.

## Haftungsausschluss

Dies ist ein Forschungsprototyp.
Die Haftung für entgangenen Gewinn, Produktionsausfall, Betriebsunterbrechung, entgangene Nutzungen, Verlust von Daten und Informationen, Finanzierungsaufwendungen sowie sonstige Vermögens- und Folgeschäden ist, außer in Fällen von grober Fahrlässigkeit, Vorsatz und Personenschäden, ausgeschlossen.

## Disclaimer of Warranty

Unless required by applicable law or agreed to in writing, Licensor provides the Work (and each Contributor provides its Contributions) on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied, including, without limitation, any warranties or conditions of TITLE, NON-INFRINGEMENT, MERCHANTABILITY, or FITNESS FOR A PARTICULAR PURPOSE.
You are solely responsible for determining the appropriateness of using or redistributing the Work and assume any risks associated with Your exercise of permissions under this License.
