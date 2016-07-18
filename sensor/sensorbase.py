import logging as log
import abc
import time
import collections
import argparse
from random import randint
from datetime import datetime
from queue import Queue

try:
    import paho.mqtt.client as mqtt
    import spidev
    import RPi.GPIO as GPIO
except ImportError:
    pass


class SensorBase ():
    
    def __init__(self, periodic = True, timeout = 120, **kwargs):
        super().__init__() # python 3 only

        #args = []
        if (kwargs['argv']): # in case using SensorBase argument parser
            args = parseargs(kwargs['argv'])
        elif (kwargs['args']): # in case using custom argument parser - please extend SensorBase parser
            args = kwargs['args']
        else:
            raise ValueError('Missing arguments on SensorBase __init__.')

        self._sensorid = args.dict['sensor_id']
        self._mqtturl = args.dict['url']
        self._mqttport = args.dict['port']
        self._mqtttopic = args.dict['topic']
        self._mqtttimeout = timeout

        self._sleeptime = kwargs['sleeptime'] # time between each value read
        self._periodic = periodic

        self._client = getMQTT(self._mqtturl, self._mqttport, self._mqtttimeout)
        self.isRunning = False
        self.run()

    def run(self):
        # reads sensor value and adds to queue
        if (not self.isRunning):
            self.isRunning = True
        else:
            return
        while (True):
            if (self._periodic):
                value = self._getSensorValue()
                self._notify_observers(value)
            time.sleep(self._sleeptime)

    @abc.abstractmethod
    def _getSensorValue(self):
        # CODE TO GET SENSOR VALUE
        pass

    def _gotSensorValue(self, value):
        self._notify_observers(value)

    def _notify_observers(self, value):
        # adds value to the queue
        dict_ = {
            "id": self._sensorid,
            "time": datetime.now(),
            "value": value,
        }
        self._client.publish(self._mqtttopic, payload=dict_["value"], qos=0, retain=False)
        #self._queue.put_nowait(dict_)

def getMQTT(url, port, timeout):
    connection_retry_interval =  timeout
    client = mqtt.Client()
    client.on_connect = on_connect

    # tries to connect for timeout seconds
    _time0 = time.time()
    while (True):
        try:
            log.info("connect:" + str(url) + ":" + str(port) + " " + str(60))
            client.connect(url, port, 60)
            return client
        except IOError as e:
            if (e.errno == 101 and ((time.time() - _time0) < connection_retry_interval)):
                log.info("error:" + str(e.errno) + " " + str(e.filename) + " " + str(e.strerror))
                time.sleep(1)
            else:
                raise

# The callback for when the client receives a CONNACK response from the server.
def on_connect(client, userdata, flags, rc):    
    log.info("on_connect:" + str(rc))

def parseargs(argv):
    parser = argparse.ArgumentParser()

    sleeptime = 1
    url = "localhost"
    port = 1883
    
    parser.add_argument('-i','--sensor-id',help='Sensor ID to be logged by this instance.', required=True)
    parser.add_argument('-u','--url',help='URL of the MQTT broker (default: "localhost").', required=False)
    parser.add_argument('-p','--port',help='Port of the MQTT broker (default: 1883).', required=False)
    parser.add_argument('-t','--topic',help='Port of the MQTT broker (default: sensor/$sensor-id).', required=False)

    args = parser.parse_args()
    args = vars(args)

    log.info("args: " + str(args))

    if ('url' not in args.keys() or args['url'] is None):
        args['url'] = url
    if ('port' not in args.keys() or args['port'] is None):
        args['port'] = port
    if ('topic' not in args.keys() or args['topic'] is None):
        args['topic'] = 'sensor/' + args['sensor_id']

    argp = collections.namedtuple('Arg', ['dict', 'parser'])
    return argp(dict = args, parser = parser)


def readadc(adcnum):
    spi = spidev.SpiDev()
    spi.open(0,0)
    # read SPI data from MCP3004 chip, 4 possible adc’s (0 thru 3)
    if ((adcnum > 3) or (adcnum < 0)):
        return -1
    r = spi.xfer2([1,8 + adcnum <<4,0])
    #print(r)
    adcout = ((r[1] & 3) << 8) + r[2]
    return adcout

'''def signal_handler(signal, frame):
    print ("Closing all threads...")
    for t in threads:
        if t.isAlive():
            t.stop()
    for t in threads:
        if t.isAlive():
            t.join()
    print ("Finished.")
    sys.exit(0)'''