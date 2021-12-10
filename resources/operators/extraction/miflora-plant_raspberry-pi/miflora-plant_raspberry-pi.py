#!/usr/bin/env python3
# -*- coding: utf-8 -*-
from mbp_client import MBPclient
import sys, json, time
##### Additional imports
from miflora.miflora_poller import MiFloraPoller
from btlewrap import BluepyBackend

def main(argv):
  # default interval for sending data (seconds)
  INTERVAL_BETWEEN_SENDING_DATA = 15
  PAR_FIELD_NAME = "name"
  PAR_FIELD_VALUE = "value"
  PAR_FIELD_NAME_INTERVAL = "interval"
  ##### Additional values
  MIFLORA_FIELD_NAME_SENSOR = "sensor"
  MIFLORA_FIELD_NAME_SENSOR_TYPE_TEMPERATURE = "temperature"
  MIFLORA_FIELD_NAME_MAC = "mac"
  targetSensor = MIFLORA_FIELD_NAME_SENSOR_TYPE_TEMPERATURE
  targetSensorMac = ""

  # instantiate the MBP client
  mbp = MBPclient()
  # initialize the MBP client
  mbp.connect()

  #### parse input arguments ######
  paramArray = json.loads(argv[0])
  #print(paramArray)
  for param in paramArray:
    if not (PAR_FIELD_NAME in param and PAR_FIELD_VALUE in param):
      continue
    elif param[PAR_FIELD_NAME].lower() == PAR_FIELD_NAME_INTERVAL.lower():
      INTERVAL_BETWEEN_SENDING_DATA = int(param[PAR_FIELD_VALUE])
    elif param[PAR_FIELD_NAME].lower() == MIFLORA_FIELD_NAME_SENSOR.lower():
      targetSensor = param[PAR_FIELD_VALUE]
    elif param[PAR_FIELD_NAME].lower() == MIFLORA_FIELD_NAME_MAC.lower():
      targetSensorMac = param[PAR_FIELD_VALUE]
  #################################

  try:
    # instantiate sensor reader of miflora
    poller = MiFloraPoller(targetSensorMac,BluepyBackend)

    # This loop ensures your code runs continuously to read values at a given interval
    while True:
      # retrieve sensor value
      received_value = poller.parameter_value(targetSensor)
      # send data to the MBP
      mbp.send_data(json.dumps({"value": float(received_value)}))
      # waits a time interval before sending new data
      time.sleep(INTERVAL_BETWEEN_SENDING_DATA)
  except:
    error = sys.exc_info()
    print ('Error reading sensor value or sending to the MBP:', str(error))

  # terminate the MBP client
  mbp.finalize()

if __name__ == "__main__":
  main(sys.argv[1:])