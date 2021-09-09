# Extraction Operator: Parametrized scripts to plann the simulation of temperature sensor data

This folder contains operator scripts for simulating sensor data of a temperature sensor, where the simulation time and number of events and anomalies can be scheduled. 

## Parameters

The following parameters need to be provided on deployment:

 - `"event" (number)`: The event to be simulated. (1 temperature rise ; 2 temperature drop)
- `"anomaly" (number)`: The anomaly to be simulated. (3: outliers; 4: wrong value type; 5: missing value; 6: no anomaly)
- `"room" (text)`: The room in which the temperature is to be measured to maintain the limits of the optimum room temperature. (a: living room b: study c: bedroom d: bathroom e: kitchen f: basement)
- `"useNewData" (switch)`: Specifies whether the last simulated data should be reused. 
- `"simTime" (text)`: Floating-Point number to define the simulation-time in hours. 
- `"amountEvents" (number)`: To define the number of events to be simulated.
- `"amountAnomalies" (number)`:  To define the number of anomalies to be simulated.

## Hardware Setup 


 - a computer running a Linux-based OS, such as a Raspberry Pi or a Laptop running the Ubuntu OS. (Optionally, you can also use a virtual machine)

## Operator files 

 - `install.sh`: This file installs the necessary libraries to run the jar file.
 
 - `start.sh`: This file starts the execution of the jar file.
 
 - `running.sh`: This file checks if the jar file is running.
  
 - `stop.sh`: This file stops the execution of the jar file.
 
 - `PlannedTempSim.jar`: This jar file contains a MQTT client, which publishes the planned simulated sensor data to a configured topic on the MBP. There are two events to be simulated: **temperature rise** and **temperature drop**. These events can be combined with an anomaly. You can choose between the following: **outlier**, **missing values**, **wrong value type**. 

 - `dataModel.json`: This file contains a data model definition which can be used for the creation of a respective operator entity in the MBP.