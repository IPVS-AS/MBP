# Multi-purpose Binding and Provisioning Platform (MBP)
This platform enables means for (i) automated binding of IoT devices in order to access their sensors and actuators, and (ii) automated software provisioning.

![MBP-UI](resources/MBP.PNG)  

How to install the MBP and use its API is explained in detail in the following:  

**[1 Installation and Configuration](#1-installation-and-configuration)**  
**[2 MBP REST API](#2-mbp-rest-api)**  

## 1 Installation and Configuration

The following sofware components are used in order to set up the MBP:  
- [Mosquitto MQTT Broker](https://mosquitto.org/download/)
- [mongoDB server](https://www.mongodb.com/download-center?jmp=nav#community)
- [InfluxDB](https://portal.influxdata.com/downloads/)
- Java8
- [Tomcat8](https://tomcat.apache.org/download-80.cgi)
- Maven
- Bootstrap template: [https://startbootstrap.com/template-overviews/sb-admin-2/](https://startbootstrap.com/template-overviews/sb-admin-2/)

### 1.1 Configuration
In order to allow the MBP to access registered devices, the devices must be configured to be accessed by SSH. Therefore, a public RSA key needs to be copied to `~./ssh` on the target device. The corresponding private key then needs to be provided when creating the device on the MBP.

### 1.2 Installation on Linux 
Please run the [installation script](install.sh), which automatically install the sofware components listed above. Once the installation is completed, the MBP will be available on the URL *http://MBP-Host:8080/MBP*.  

### 1.3 Installation on Windows
Please execute the following steps:  
- Install and start [Mosquitto MQTT Broker](https://mosquitto.org/download/), [mongoDB server](https://www.mongodb.com/download-center?jmp=nav#community), [InfluxDB](https://portal.influxdata.com/downloads/) and [Tomcat8](https://tomcat.apache.org/download-80.cgi)   
- Create the *MBP.war* file by building the provided maven project
    
    `$ mvn clean install`  
    
- Deploy the MBP application on Tomcat by moving `MBP.war` to the Tomcat `webapps` folder  

Once the installation is completed, the MBP will be available on the URL *http://MBP-Host:8080/MBP*.  

### 1.4 Using the MBP
The MBP provides two user roles, *expert* and *normal*. As an *expert user*, it is possible to register *Adapters*, *Devices*, *Sensors* and *Actuators*. As a *normal user* it is possible to register only *Sensors* and *Actuators*.

In order to register a sensor or an actuator, the correponding *Adapter* and *Device* must be registered before the sensor or actuator.

Once a sensor is registered, pushing sensor data to the MBP, or receiving sensor data, can be done through a MQTT Client. Sensor topics follows the structure 'sensor/$sensor_id' while actuators follows the structure 'actuator/$actuator_id'. The *ids* are generated on the registration step and are shown in the MBP UI.

The message format is a json string containing at least the following elements:
 - "component" : [ "SENSOR", "ACTUATOR" ] <- one of the values, accordingly
 - "id" : $id <- sensor or actuator id
 - "value" : $value

For example,

    
    $ mosquitto_pub.exe -t sensor/596cafaa6c0ccd5d29da0e90 
      -m '{"component": "SENSOR", 
           "id": "596cafaa6c0ccd5d29da0e90", 
	   "value": 20}'
    
### 1.5 Improving startup time
In order to decrease the time that is required by Tomcat to make the MBP web app available again after a reboot of the hosting system, it is helpful to adjust the 'java.security' file of the JRE as suggested [in this post](https://stackoverflow.com/a/26432537). Otherwise it may take up to 30 minutes until the MBP can be accessed again. The installation script 'install.sh' takes automatically care of this.
	
## 2 MBP REST API
A REST API for the management of devices, adapters, sensors and actuators is provided. Furthermore, an interface to trigger the binding of sensors and actuator is as well provided. Finally, sensor and actuator values sent to the MBP can be retrieved.

Basic Authentication is needed to use the REST API and to access the resources. For this reason, an authorization header is added in every client request except for the POST requests "/api/authenticate" and "/api/users", which are used to authenticate and create a new user. The authorization header looks like this:
``
Authorization: Basic YWRtaW46YWRtaW4=
``
The value "YWRtaW46YWRtaW4=" is a Base64 encoding of "admin:admin" (username:password).

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

Click [here](https://github.com/IPVS-AS/MBP/wiki/API-Reference) to see the API Reference.

## Haftungsausschluss

Dies ist ein Forschungsprototyp.
Die Haftung für entgangenen Gewinn, Produktionsausfall, Betriebsunterbrechung, entgangene Nutzungen, Verlust von Daten und Informationen, Finanzierungsaufwendungen sowie sonstige Vermögens- und Folgeschäden ist, außer in Fällen von grober Fahrlässigkeit, Vorsatz und Personenschäden, ausgeschlossen.

## Disclaimer of Warranty

Unless required by applicable law or agreed to in writing, Licensor provides the Work (and each Contributor provides its Contributions) on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied, including, without limitation, any warranties or conditions of TITLE, NON-INFRINGEMENT, MERCHANTABILITY, or FITNESS FOR A PARTICULAR PURPOSE.
You are solely responsible for determining the appropriateness of using or redistributing the Work and assume any risks associated with Your exercise of permissions under this License.
