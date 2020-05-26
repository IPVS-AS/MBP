# MBP Operators Repository

This folder contains examplary operator scripts that can be deployed onto IoT devices by the MBP. 
These operators are divided into four categories: 

 - [extraction](extraction) operators: this folder contains operator scripts to bind sensors to the MBP. 

 - [control](control) operators: this folder contains operator scripts to bind actuators to the MBP and also to control them through the MBP. 

 - [monitoring](monitoring) operators: this folder contains operator scripts to monitor IoT devices. 

 - [processing](processing) operators: this folder contains operator scripts to execute operations onto IoT devices. 

In order to facilitate the development of such operators, we provide the [mbp_client](mbp_client), a python-based library, which provide functions to connect and communicate to the MBP in a simple manner.  

The MBP enables users to provide their own operators, which can be implemented in any programming language.
The MBP requires, however, to be provided with specific lifecycle management scripts for the operator (i.e., `install.sh`, `start.sh`, `running.sh`, and `stop.sh`), in order to be able to automate the deployment of the user-defined operators.
The application logic of these management scripts can, however, still be defined by the user, e.g., by specifying necessary software that needs to be installed. 

## Haftungsausschluss

Dies ist ein Forschungsprototyp.
Die Haftung für entgangenen Gewinn, Produktionsausfall, Betriebsunterbrechung, entgangene Nutzungen, Verlust von Daten und Informationen, Finanzierungsaufwendungen sowie sonstige Vermögens- und Folgeschäden ist, außer in Fällen von grober Fahrlässigkeit, Vorsatz und Personenschäden, ausgeschlossen.

## Disclaimer of Warranty

Unless required by applicable law or agreed to in writing, Licensor provides the Work (and each Contributor provides its Contributions) on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied, including, without limitation, any warranties or conditions of TITLE, NON-INFRINGEMENT, MERCHANTABILITY, or FITNESS FOR A PARTICULAR PURPOSE.
You are solely responsible for determining the appropriateness of using or redistributing the Work and assume any risks associated with Your exercise of permissions under this License.
