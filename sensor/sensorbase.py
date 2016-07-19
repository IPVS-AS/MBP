import logging as log
import abc
import time
import collections
import argparse
import json
from random import randint
from datetime import datetime
from queue import Queue

try:
    import paho.mqtt.client as mqtt
    import spidev
    import RPi.GPIO as GPIO
except ImportError:
    pass

_q = Queue()

class SensorBase:
    
    def __init__(self, periodic = True, actuator = False, **kwargs):
        super().__init__() # python 3 only

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
        self._mqtttimeout = args.dict['timeout']

        self._pinset = args.dict['pinset']
        self._sleeptime = args.dict['sleeptime'] # time between each value read

        self._periodic = periodic
        self._actuator = actuator

        self._client = getMQTT(self._mqtturl, self._mqttport, self._mqtttimeout)
        #if (self._actuator):
        self._client.subscribe(self._mqtttopic, qos=0)
        self.isRunning = False
        self.run()

    def run(self):
        # reads sensor value and adds to queue
        if (not self.isRunning):
            self.isRunning = True
        else:
            return

        if (self._periodic):
            self._client.loop_start()
            while (True):    
                value = self._getSensorValue()
                self._notify_observers(value)
                time.sleep(self._sleeptime)
            self._client.loop_stop()
        elif (self._actuator):
            self._client.loop_start()
            while (True):
                payload = _q.get()
                val = collections.namedtuple('Val', ['payload', 'value'])
                value = None
                try:
                    value = json.loads(payload.decode('utf-8'))['value']
                except:
                    pass
                self._setActuatorValue(val(payload = payload, value = value))
            self._client.loop_stop()
        else:
            self._client.loop_forever()

    @abc.abstractmethod
    def _getSensorValue(self):
        # CODE TO GET SENSOR VALUE
        pass

    @abc.abstractmethod
    def _setActuatorValue(self, payload):
        # CODE TO GET SENSOR VALUE
        pass

    def _gotSensorValue(self, value):
        self._notify_observers(value)

    def _notify_observers(self, value):
        # adds value to the queue
        dict_ = {
            "id": self._sensorid,
            "time": str(datetime.now()),
            "value": value,
        }
        json_ = json.dumps(dict_)
        self._client.publish(self._mqtttopic, payload=str(json_), qos=0, retain=False)

        # The callback for when the client receives a CONNACK response from the server.


def on_message(client, userdata, message):
    log.info("on_message:" + str(message))
    _q.put_nowait(message.payload)

def on_connect(client, userdata, rc):
    log.info("on_connect:" + str(rc))

def on_publish(client, userdata, mid):
    pass

def on_subscribe(client, userdata, mid, granted_qos):
    pass

def getMQTT(url, port, timeout):
    connection_retry_interval =  timeout
    client = mqtt.Client()
    client.on_connect = on_connect
    client.on_message = on_message
    client.on_subscribe = on_subscribe
    client.on_publish = on_publish

    # tries to connect for timeout seconds
    _time0 = time.time()
    while (True):
        try:
            log.info("connect:" + str(url) + ":" + str(port) + " " + str(60))
            client.connect(url, port, 60)
            return client
        except IOError as e:
            if (connection_retry_interval > 0):
                if (e.errno == 101 and ((time.time() - _time0) < connection_retry_interval)):
                    log.info("error:" + str(e.errno) + " " + str(e.filename) + " " + str(e.strerror))
                    time.sleep(1)
                else:
                    raise

def parseargs(argv):
    parser = argparse.ArgumentParser()

    sleeptime = 1
    url = "localhost"
    port = 1883
    timeout = 0
    pinset = '26,27'
    
    parser.add_argument('-i','--sensor-id', help='Sensor ID to be logged by this instance.', required=True)
    parser.add_argument('-u', '--url', help='URL of the MQTT broker (default: "localhost").', required=False)
    parser.add_argument('--port', help='Port of the MQTT broker (default: 1883).', required=False)
    parser.add_argument('-t', '--topic',help='Port of the MQTT broker (default: sensor/$sensor-id).', required=False)
    parser.add_argument('-p', '--pinset', help='List of pins separated by commas (default: "26,27").', required=False)
    parser.add_argument('-s', '--sleeptime', type=str, help='Interval in seconds between each reading loop if periodic (default: 1).', required=False)
    parser.add_argument('--timeout', help='MQTT broker connection timeout in seconds. 0 is infinite (default: 0).', required=False)

    args = parser.parse_args()
    args = vars(args)

    log.info("args: " + str(args))

    if ('url' not in args.keys() or args['url'] is None):
        args['url'] = url
    if ('port' not in args.keys() or args['port'] is None):
        args['port'] = port
    if ('topic' not in args.keys() or args['topic'] is None):
        args['topic'] = 'sensor/' + args['sensor_id']
    if ('timeout' not in args.keys() or args['timeout'] is None):
        args['timeout'] = timeout
    if ('sleeptime' not in args.keys() or args['sleeptime'] is None):
        args['sleeptime'] = sleeptime
    if ('pinset' not in args.keys() or args['pinset'] is None):
        args['pinset'] = pinset
    args['pinset'] = map(int, args['pinset'].split(','))

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
