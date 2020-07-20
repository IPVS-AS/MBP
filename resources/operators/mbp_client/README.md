# How to use the MBP client

The MBP client corresponds to a python script named [`mbp_client.py`](mbp_client.py). This script contains the logic to connect and communicated with the MBP. It abstracts a MQTT client and further configuration steps.

To create your **own** python-based operator, you just need to import the MBP client in your customized python script:

```
  from mbp_client import MBPclient 
```

 An example of how to use the MBP client is provided in [`mbp_client_usage_example.py`](mbp_client_usage_example.py):  
 
 - `mbp = MBPclient()` instantiates the MBP client.  
 - `mbp.connect()` iniatializes and connect to the MBP.  
 - `mbp.send_data(value)`sends sensor values to the MBP.  
 - `mbp.finalize()` terminates the MBP client.  

Furthermore, to manage the life cycle of the operator, we provide generic scripts that are executed by the MBP automatically: 
 - [`install.sh`](install.sh)  
 - [`start.sh`](start.sh)  
 - [`running.sh`](running.sh)  
 - [`stop.sh`](stop.sh)  

These management scripts are required to be provided at the registration of an operator in the MBP, however, you still can change their content as you need for handle your operator.  

 **However**, to let the MBP and the `start.sh` know the **entry point** of your operator, i.e., your main python script, you have to indicate its name in the content of `entry-file-name.txt`, for example, `mbp_client_usage_example.py`. This is **important** in case you provide several python scripts, so that the MBP can know which one should be started.

# Summary: operator files list

In summary, your operator should contain the following files:

 - `mbp_client.py`: This python script contains the logic to connect and to communicate with the MBP. It abstracts a MQTT client and further configuration steps.  
 - `entry-file-name`: This file contains solely the name of your main python script including its extension.  
 - `<your-own-main-script>.py`: This file is your python script that uses the `mbp_client` to send (or receive) values to the MBP.  
 - `<additional-script>.py` (0 or more): Further scripts that you might need to use in your main python script.  
 - `install.sh`: This file installs the necessary libraries to run the `mbp_client` and the main python script.  
 - `start.sh`: This file starts the execution of the main python script, the one indicated in the `entry-file-name`.  
 - `running.sh`: This file checks if the main python script is running.  
 - `stop.sh`: This file stops the execution of the main python script.  
 