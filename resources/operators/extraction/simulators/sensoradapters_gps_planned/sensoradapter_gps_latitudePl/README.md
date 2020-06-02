# Extraction Operator: Parametrized scripts for a planned simulation of the X-axis (Latitude) of a GPS sensor

This folder contains operator scripts for a planned simulation of the data of the X-axis (Latitude) of a GPS sensor. The simulator can be used in the case of an IoT-Application where actions should be triggered on approaching or moving away from a  Smart Home.     

## Parameters
The following parameters need to be provided on deployment:

 - `"event" (number)`: The event to be simulated. (1: Move away; 2: Move to the Smart Home )
- `"anomaly" (number)`: The anomaly to be simulated. (3: outlier; 4: missing values; 5: wrong value type; 6: no anomaly)
- `"who" (text)`: Specifies whether the GPS data of a cat or a person should be simulated. (a: person; b: cat)
- `"latitude" (text)`: Latitude of the Smart Home (Floating-Point number). 
- `"longitude" (text)`: Longitude of the Smart Home (Floating-Point number). 
- `"hight" (text)`: Hight of the Smart Home (Floating-Point number). 
- `"useNewData" (switch)`: Specifies whether the last simulated data should be reused. 
- `"reactionMeters" (number)`: Radius according to which the IoT application should react.
- `"randomAngle" (number)`:  Direction to move away from or to the house (0-360°).
- `"axis" (number)`: Axis on which the anomaly is to be simulated. (1: X-Axis; 2: Y-Axis, 3: Z-Axis)
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
 
 - `sensoradapter_gpspl_latitude.jar`: This jar file contains a MQTT client, which publishes the planned simulated sensor data to a configured topic on the MBP. There are two events to be simulated: **Move away and to the Smart Home**. These events can be combined with an anomaly. You can choose between the following: **outlier, missing values, wrong value type, no anomaly**. Simulates values of the X-axis (Latitude). 