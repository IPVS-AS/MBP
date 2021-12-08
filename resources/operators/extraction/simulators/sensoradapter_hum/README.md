# Extraction Operator: Parametrized scripts to simulate humidity sensor data

This folder contains operator scripts to simulate sensor data of a humidity sensor. 

## Parameters

The following parameters need to be provided on deployment:

 - `"event" (number)`: The event to be simulated. (1 humidity rise ; 2 humidity drop)
- `"anomaly" (number)`: The anomaly to be simulated. (3: outliers; 4: wrong value type; 5: missing value; 6: no anomaly)
- `"room" (text)`: The room in which the temperature is to be measured to maintain the limits of the optimum room temperature. (a: living room b: study c: bedroom d: bathroom e: kitchen f: basement)
- `"useNewData" (switch)`: Specifies whether the last simulated data should be reused. 

## Hardware Setup 


 - a computer running a Linux-based OS, such as a Raspberry Pi or a Laptop running the Ubuntu OS. (Optionally, you can also use a virtual machine)

## Operator files 

 - `install.sh`: This file installs the necessary libraries to run the jar file.
 
 - `start.sh`: This file starts the execution of the jar file.
 
 - `running.sh`: This file checks if the jar file is running.
  
 - `stop.sh`: This file stops the execution of the jar file.
 
 - `HumSim.jar`: This jar file contains a MQTT client, which publishes the simulated sensor data to a configured topic on the MBP. There are two events to be simulated: **humidity rise** and **humidity drop**. These events can be combined with an anomaly. You can choose between the following: **outlier**, **missing values**, **wrong value type**. 

 - `dataModel.json`: This file contains a data model definition which can be used for the creation of a respective operator entity in the MBP.