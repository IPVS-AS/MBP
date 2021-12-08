#!/usr/bin/env python3
from mbp_client import MBPclient  # for connecting to the MBP
from mbp_analog_reader import AnalogInputReader # for reading analog sensor data
import sys, json, time

DEFAULT_INTERVAL_FOR_SENDING_DATA = 30 # default interval for sending (seconds)
DEFAULT_CHANNEL = 4 # default channel where sensor is connected

def avg(lst):
  if(len(lst) < 1):
    return 0.0
  return sum(lst)/len(lst)

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

    last_20_values = []
    last_send_time = time.perf_counter()
    while True:
      # retrieve sensor value
      received_value = reader.read_sound()
      # waits a time interval before sending new data
      # time.sleep(int(interval))
      if(avg(last_20_values) < received_value):
        if(len(last_20_values) == 20):
          last_20_values.pop(0)
        last_20_values.append(received_value)
      last_20_values.append(received_value)  
      now = time.perf_counter()
      if(now - last_send_time >= interval):
        # send data to the MBP
        mbp.send_data(json.dumps({"sound-avg-val": avg(last_20_values)}))
        last_send_time = time.perf_counter()
        last_20_values = []
      time.sleep(0.05)
  except:
    error = sys.exc_info()
    print ('Error reading sensor value or sending to the MBP:', str(error))

  # terminate the MBP client
  mbp.finalize()

if __name__ == "__main__":
  main(sys.argv[1:])