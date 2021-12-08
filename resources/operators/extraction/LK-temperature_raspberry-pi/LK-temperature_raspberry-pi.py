#!/usr/bin/env python3
from mbp_client import MBPclient  # for connecting to the MBP
from mbp_analog_reader import AnalogInputReader # for reading analog sensor data
import sys, json, time

DEFAULT_INTERVAL_FOR_SENDING_DATA = 30 # default interval for sending (seconds)
DEFAULT_CHANNEL = 0 # default channel where sensor is connected

def main(argv):
  # instantiate the MBP client
  mbp = MBPclient()
  # initialize the MBP client
  mbp.connect()

  # parse input arguments
  interval = mbp.get_start_par_value(argv, "interval", DEFAULT_INTERVAL_FOR_SENDING_DATA)
  adc_channel = mbp.get_start_par_value(argv, "channel", DEFAULT_CHANNEL)

  try:
    # Hardware - init analog input reader
    reader = AnalogInputReader(adc_channel)

    while True:
      # retrieve sensor value
      received_value = reader.read_temperature()
      # send data to the MBP
      mbp.send_data(json.dumps({"temperature": float(received_value)}))
      # waits a time interval before sending new data
      time.sleep(int(interval))
  except:
    error = sys.exc_info()
    print ('Error reading sensor value or sending to the MBP:', str(error))

  # terminate the MBP client
  mbp.finalize()

if __name__ == "__main__":
  main(sys.argv[1:])