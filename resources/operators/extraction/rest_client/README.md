# Extraction Operator: Scripts for extracting values via REST requests

This folder contains scripts for an extraction operator that performs periodically REST requests against a HTTP endpoint
and extracts a value from its JSON response. The address of the endpoint, the HTTP method to use, the value that 
is supposed to be extracted from the response and the delay between requests can be specified via parameters.

## Hardware Setup 

 - A computer running a Linux-based OS, such as a Raspberry Pi or a laptop

## Operator files 
 - `mbp_client.py`: This python script contains the logic to connect and to communicate with the MBP. It abstracts a MQTT client and further configuration steps.  
 - `entry-file-name`: This file contains solely the name of your main python script including its extension.  
 - `rest_client.py`: This python script executes the HTTP requests and extracts the desired value from the responses. It uses the `mbp_client` to send these data to the MBP.  
 - `install.sh`: This file installs the necessary libraries to run the `mbp_client` and the main python script.  
 - `start.sh`: This file starts the execution of the main python script, the one indicated in the `entry-file-name`.  
 - `running.sh`: This file checks if the main python script is running.  
 - `stop.sh`: This file stops the execution of the main python script.
 
 ## Parameters
 - `url`: The URL of the endpoint against which the HTTP request is supposed to be issued
 - `method`: The HTTP method to use for the requests, defaults to `get` (optional)
 - `jsp_exp`: A [JSONPath expression](https://pypi.org/project/jsonpath-ng/) for extracting the desired value from the response to the HTTP request
 - `interval`: The time interval (in seconds) between the requests
 
 