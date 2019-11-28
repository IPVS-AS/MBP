# MBP Operators Repository

This folder contains examplary operator scripts that can be deployed onto IoT devices by the MBP. These operators are divided into four categories: 

 - [extraction](extraction) operators: this folder contains operator scripts to bind sensors to the MBP. 

 - [control](control) operators: this folder contains operator scripts to bind actuators to the MBP and also to control them through the MBP. 

 - [monitoring](monitoring) operators: this folder contains operator scripts to monitor IoT devices. 

 - [processing](processing) operators: this folder contains operator scripts to execute operations onto IoT devices. 

The MBP enables users to provide their own operators, which can be implemented in any programming language.
The MBP requires, however, to be provided with specific lifecycle management scripts for the operator (i.e., *install.sh*, *start.sh*, *running.sh*, and *stop.sh*), in order to be able to automate the deployment of the user-defined operators.
The application logic of these management scripts can, however, still be defined by the user, e.g., by specifying necessary software that needs to be installed. 