# Extraction Operator: Parametrized scripts plann the simulation of the data of a Y-axis of a acceleration sensor

This folder contains operator scripts for a planned simulation of sensor data of the Y-axis of an acceleration sensor, where the simulation time and number of events and anomalies can be scheduled. The simulator can be used in the context of an alarm sytem, where a object has to be observed.

## Parameters

The following parameters need to be provided on deployment:

 - `"event" (number)`: The event to be simulated. (1: Object in idle state; 2: Object in motion)
- `"anomaly" (number)`: The anomaly to be simulated. (3: fly bumps into the object; 4: outlier; 5: wrong value type; 6: no anomaly)
- `"weightObject" (number)`: The weight of the object to be observed.
- `"sensitivityClass" (number)`: Sensitivity class to where other vibrations should be ignored. (0: Immediate reaction: 0.0g; 1: imperceptible: <0.001g; 2: very light: 0.001-0.002g; 3: light: 0,002-0,005g)
- `"reactionMeters" (number)`: After how many meters of acceleration an action should be executed within the application. 
- `"useNewData" (switch)`: Specifies whether the last simulated data should be reused. 
- `"directionAnomaly" (number)`: The direction in which the anomaly is to be simulated. (0: up; 1: down; 2: right; 3: left; 4: forward; 5: backward)
-  `"directionMovement" (number)`: The direction in which a movement is to be simulated. (0: up; 1: down; 2: right; 3: left; 4: forward; 5: backward)
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
 
 - `AccYPlSim.jar`: This jar file contains a MQTT client, which publishes the simulated sensor data to a configured topic on the MBP. There are two events to be simulated: **Object in idle state** and **Object in motion**. These events can be combined with an anomaly. You can choose between the following: **fly bumps into the object, outlier, wrong value type, no anomaly**. Simulates values of the Y-axis.