/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.citpot.sensmonqtt.ssh;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author rafaelkperes
 */
public class ScriptDeployerTest {

    public static String key
            = "-----BEGIN RSA PRIVATE KEY-----\n"
            + "MIIEogIBAAKCAQEAyGALfW0RP//eXFfhKfVcQK8rCCxymWBduf0rmMmDApN50Kzv\n"
            + "ESS955Y8HWvTPGDwd0ny6rthWcbDRF2+2J2AsKa+UnrXamZ3PdOfIPmuCFSigiQd\n"
            + "fnjFk8Zg8sdtywBCBy2SHwq7QBsZME2Aztyx3L4k4lk2VK8w+2F9gCmAVxY+KLDN\n"
            + "Da5NsgVEe9xVvvzhwkmf86T6r4dhYmWPgzW30GkUh4vvBvozBbfa0YV/vj4f1DP0\n"
            + "U3l91wiUl96Ag0e7r2wsCuufW6Gs8Gy1IE/CpAbyrUxrH+yDoNFur0QP7qDiioRR\n"
            + "X7p+HpCdhl3qKKB6CeflpQOlKpx7Pj87QhL0LQIDAQABAoIBACzWWRva8RY6Ij7V\n"
            + "p1vlPJx41g9BKu+pQa/huAS7auaDq6mHWQOkDh6pXpBS1XTYWFbJJGNkRLd7I6zD\n"
            + "sXX1YJum5EW+mT+E6D/cf+o4FLpmferTPApV6hhUNtN8ztOzHhNPHjh2BUqmBa/q\n"
            + "V91yQxabMdO4lNDEVxiZSyUHpGFYAj4odQVJvGRG2502L0BKyYeMABmtZrKjaS5K\n"
            + "aahbL0Z2pkQ+gakEn+1cb/Rd2IDQhrA6EpacK9reoWydpUxP/MReQdeMU62rwqFe\n"
            + "TpEPc6ZS19XxWKyIhHHLiZl7qNcXkCOK64kEgvlark9miNj3JUf9P0OAmElRAtdM\n"
            + "PXP6Qf0CgYEA8mwnIyJ1atBsqgTySD6X+dTPtHUiSJ8euOtiqQTH8t2MTU06mZuA\n"
            + "8e7Fy45yxKSQ7w6uJA9UJs2Ru2vN6lC6zav0ri4LXhv2VAwJFkQKDv2fSR1lOAbk\n"
            + "/cKnwoWNSqda+lq+Bl7ZiLxSeviYbus+LcgIq3HyBVmcvKpIJRN/tYMCgYEA05kE\n"
            + "2fI5/dnyH1MvLCoSKkYp40uUwatnSDt07WSqa3SH5E/uz4lasFcgeJSmqOYpa3tw\n"
            + "/bqBXNlqWCWI6oNi/23Pv4mj69EFrfSf85IQcms8dGStdcin+9VmYSJpn+QPgia/\n"
            + "n4vm125CQrURmuE2r+oOcV3ShcpO1lS4AMs8MI8CgYBoFi3btRDrMuBlQ8hvYojI\n"
            + "WSpxVhXJTqDXTyHGZmofiiaSjkVJ7O25cwb0No5qhipAqnH0w6wjGQKokUoRgGYk\n"
            + "pt9g5h41YxYp0h0YtVAITbdVokxyeOtbVXfIWqVm12KFue57N8B5KDrV1+VDQrgo\n"
            + "2gl26266A1b73rUpTiz4VwKBgHgUtrQYyuBM9yLfyj1+AqELAGqFUf42j35mf4zZ\n"
            + "O/2PPC9NTXFpuZWpXDwR4CKpu4fLnevgE9nlaHxtkK3FskDSyLsiGWySSm7WDI/l\n"
            + "rH/Ca6SCHg5huTMpf9hP9zFN858g7k5UzsQjRmck6sDCXo6mfVvIqthSXzszCNkq\n"
            + "fRXxAoGARRp2fahKz31kUOVprVSK2UsH340fET43X3QlygyNI33J4V6tYUpTgCY7\n"
            + "dyBUmBHZKeZwJYYAtfkI4ACDCI0KEa6NdzAtwcwUgsR10fh6jGGBrKT88F4C5Xe1\n"
            + "8JinHG8VObUcB1S7+vmct88/ELxa+9CnJ/NbiYyDw0cuAxqWUWg=\n"
            + "-----END RSA PRIVATE KEY-----";

    public static String pubkey
            = "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQDIYAt9bRE//95cV+Ep9VxArysILH"
            + "KZYF25/SuYyYMCk3nQrO8RJL3nljwda9M8YPB3SfLqu2FZxsNEXb7YnYCwpr5Setdq"
            + "Znc9058g+a4IVKKCJB1+eMWTxmDyx23LAEIHLZIfCrtAGxkwTYDO3LHcviTiWTZUrz"
            + "D7YX2AKYBXFj4osM0Nrk2yBUR73FW+/OHCSZ/zpPqvh2FiZY+DNbfQaRSHi+8G+jMF"
            + "t9rRhX++Ph/UM/RTeX3XCJSX3oCDR7uvbCwK659boazwbLUgT8KkBvKtTGsf7IOg0W"
            + "6vRA/uoOKKhFFfun4ekJ2GXeoooHoJ5+WlA6UqnHs+PztCEvQt pi@raspberrypi";

    public ScriptDeployerTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of deployScript method, of class ScriptDeployer.
     */
    @Test
    public void testDeployScript() throws Exception {
        System.out.println("deployScript");
        String sensorbase = "import logging as log\n"
                + "import abc\n"
                + "import time\n"
                + "import collections\n"
                + "import argparse\n"
                + "import json\n"
                + "from random import randint\n"
                + "from datetime import datetime\n"
                + "from queue import Queue\n"
                + "\n"
                + "try:\n"
                + "    import paho.mqtt.client as mqtt\n"
                + "    import spidev\n"
                + "    import RPi.GPIO as GPIO\n"
                + "except ImportError:\n"
                + "    pass\n"
                + "\n"
                + "_q = Queue()\n"
                + "\n"
                + "class SensorBase:\n"
                + "    \n"
                + "    def __init__(self, periodic = True, actuator = False, **kwargs):\n"
                + "        super().__init__() # python 3 only\n"
                + "\n"
                + "        if (kwargs['argv']): # in case using SensorBase argument parser\n"
                + "            args = parseargs(kwargs['argv'])\n"
                + "        elif (kwargs['args']): # in case using custom argument parser - please extend SensorBase parser\n"
                + "            args = kwargs['args']\n"
                + "        else:\n"
                + "            raise ValueError('Missing arguments on SensorBase __init__.')\n"
                + "\n"
                + "        self._sensorid = args.dict['sensor_id']\n"
                + "        self._mqtturl = args.dict['url']\n"
                + "        self._mqttport = args.dict['port']\n"
                + "        self._mqtttopic = args.dict['topic']\n"
                + "        self._mqtttimeout = args.dict['timeout']\n"
                + "\n"
                + "        self._pinset = args.dict['pinset']\n"
                + "        self._sleeptime = args.dict['sleeptime'] # time between each value read\n"
                + "\n"
                + "        self._periodic = periodic\n"
                + "        self._actuator = actuator\n"
                + "\n"
                + "        self._client = getMQTT(self._mqtturl, self._mqttport, self._mqtttimeout)\n"
                + "        #if (self._actuator):\n"
                + "        self._client.subscribe(self._mqtttopic, qos=0)\n"
                + "        self.isRunning = False\n"
                + "        #self.run()\n"
                + "\n"
                + "    def run(self):\n"
                + "        # reads sensor value and adds to queue\n"
                + "        if (not self.isRunning):\n"
                + "            self.isRunning = True\n"
                + "        else:\n"
                + "            return\n"
                + "\n"
                + "        if (self._periodic):\n"
                + "            self._client.loop_start()\n"
                + "            while (True):    \n"
                + "                value = self._getSensorValue()\n"
                + "                self._notify_observers(value)\n"
                + "                time.sleep(self._sleeptime)\n"
                + "            self._client.loop_stop()\n"
                + "        elif (self._actuator):\n"
                + "            self._client.loop_start()\n"
                + "            while (True):\n"
                + "                payload = _q.get()\n"
                + "                val = collections.namedtuple('Val', ['payload', 'value'])\n"
                + "                value = None\n"
                + "                try:\n"
                + "                    value = json.loads(payload.decode('utf-8'))['value']\n"
                + "                except:\n"
                + "                    pass\n"
                + "                self._setActuatorValue(val(payload = payload, value = value))\n"
                + "            self._client.loop_stop()\n"
                + "        else:\n"
                + "            self._client.loop_forever()\n"
                + "\n"
                + "    @abc.abstractmethod\n"
                + "    def _getSensorValue(self):\n"
                + "        # CODE TO GET SENSOR VALUE\n"
                + "        pass\n"
                + "\n"
                + "    @abc.abstractmethod\n"
                + "    def _setActuatorValue(self, payload):\n"
                + "        # CODE TO GET SENSOR VALUE\n"
                + "        pass\n"
                + "\n"
                + "    def _gotSensorValue(self, value):\n"
                + "        self._notify_observers(value)\n"
                + "\n"
                + "    def _notify_observers(self, value):\n"
                + "        # adds value to the queue\n"
                + "        dict_ = {\n"
                + "            \"id\": self._sensorid,\n"
                + "            \"time\": str(datetime.now()),\n"
                + "            \"value\": str(value),\n"
                + "        }\n"
                + "        json_ = json.dumps(dict_)\n"
                + "        self._client.publish(self._mqtttopic, payload=str(json_), qos=0, retain=False)\n"
                + "\n"
                + "        # The callback for when the client receives a CONNACK response from the server.\n"
                + "\n"
                + "    def getPinset(self):\n"
                + "        return self._pinset\n"
                + "\n"
                + "def on_message(client, userdata, message):\n"
                + "    log.info(\"on_message:\" + str(message))\n"
                + "    _q.put_nowait(message.payload)\n"
                + "\n"
                + "def on_connect(client, userdata, rc):\n"
                + "    log.info(\"on_connect:\" + str(rc))\n"
                + "\n"
                + "def on_publish(client, userdata, mid):\n"
                + "    pass\n"
                + "\n"
                + "def on_subscribe(client, userdata, mid, granted_qos):\n"
                + "    pass\n"
                + "\n"
                + "def getMQTT(url, port, timeout):\n"
                + "    connection_retry_interval =  timeout\n"
                + "    client = mqtt.Client()\n"
                + "    client.on_connect = on_connect\n"
                + "    client.on_message = on_message\n"
                + "    client.on_subscribe = on_subscribe\n"
                + "    client.on_publish = on_publish\n"
                + "\n"
                + "    # tries to connect for timeout seconds\n"
                + "    _time0 = time.time()\n"
                + "    while (True):\n"
                + "        try:\n"
                + "            log.info(\"connect:\" + str(url) + \":\" + str(port) + \" \" + str(60))\n"
                + "            client.connect(url, port, 60)\n"
                + "            return client\n"
                + "        except IOError as e:\n"
                + "            if (connection_retry_interval > 0):\n"
                + "                if (e.errno == 101 and ((time.time() - _time0) < connection_retry_interval)):\n"
                + "                    log.info(\"error:\" + str(e.errno) + \" \" + str(e.filename) + \" \" + str(e.strerror))\n"
                + "                    time.sleep(1)\n"
                + "                else:\n"
                + "                    raise\n"
                + "\n"
                + "def parseargs(argv):\n"
                + "    parser = argparse.ArgumentParser()\n"
                + "\n"
                + "    sleeptime = 1\n"
                + "    url = \"localhost\"\n"
                + "    port = 1883\n"
                + "    timeout = 0\n"
                + "    pinset = '26,27'\n"
                + "    \n"
                + "    parser.add_argument('-i','--sensor-id', help='Sensor ID to be logged by this instance.', required=True)\n"
                + "    parser.add_argument('-u', '--url', help='URL of the MQTT broker (default: \"localhost\").', required=False)\n"
                + "    parser.add_argument('--port', help='Port of the MQTT broker (default: 1883).', required=False)\n"
                + "    parser.add_argument('-t', '--topic',help='Port of the MQTT broker (default: sensor/$sensor-id).', required=False)\n"
                + "    parser.add_argument('-p', '--pinset', help='List of pins separated by commas (default: \"26,27\").', required=False)\n"
                + "    parser.add_argument('-s', '--sleeptime', type=str, help='Interval in seconds between each reading loop if periodic (default: 1).', required=False)\n"
                + "    parser.add_argument('--timeout', help='MQTT broker connection timeout in seconds. 0 is infinite (default: 0).', required=False)\n"
                + "\n"
                + "    args = parser.parse_args()\n"
                + "    args = vars(args)\n"
                + "\n"
                + "    log.info(\"args: \" + str(args))\n"
                + "\n"
                + "    if ('url' not in args.keys() or args['url'] is None):\n"
                + "        args['url'] = url\n"
                + "    if ('port' not in args.keys() or args['port'] is None):\n"
                + "        args['port'] = port\n"
                + "    if ('topic' not in args.keys() or args['topic'] is None):\n"
                + "        args['topic'] = 'sensor/' + args['sensor_id']\n"
                + "    if ('timeout' not in args.keys() or args['timeout'] is None):\n"
                + "        args['timeout'] = timeout\n"
                + "    if ('sleeptime' not in args.keys() or args['sleeptime'] is None):\n"
                + "        args['sleeptime'] = sleeptime\n"
                + "    if ('pinset' not in args.keys() or args['pinset'] is None):\n"
                + "        args['pinset'] = pinset\n"
                + "    args['pinset'] = list(map(int, args['pinset'].split(',')))\n"
                + "\n"
                + "    argp = collections.namedtuple('Arg', ['dict', 'parser'])\n"
                + "    return argp(dict = args, parser = parser)\n"
                + "\n"
                + "\n"
                + "def readadc(adcnum):\n"
                + "    spi = spidev.SpiDev()\n"
                + "    spi.open(0,0)\n"
                + "    # read SPI data from MCP3004 chip, 4 possible adc’s (0 thru 3)\n"
                + "    if ((adcnum > 3) or (adcnum < 0)):\n"
                + "        return -1\n"
                + "    r = spi.xfer2([1,8 + adcnum <<4,0])\n"
                + "    #print(r)\n"
                + "    adcout = ((r[1] & 3) << 8) + r[2]\n"
                + "    return adcout";

        String sensorstub = "import sys\n"
                + "from random import randint\n"
                + "from sensorbase import SensorBase\n"
                + "\n"
                + "\n"
                + "class SensorStub (SensorBase):\n"
                + "\n"
                + "    def __init__(self, argv):\n"
                + "        super().__init__(argv = argv, periodic = True) # python 3 only\n"
                + "\n"
                + "    def _getSensorValue(self):\n"
                + "        return randint(0,9)\n"
                + "\n"
                + "class SensorWithCallback (SensorBase):\n"
                + "\n"
                + "    def __init__(self, argv, periodic = False):\n"
                + "        super().__init__(argv = argv, periodic = False) # python 3 only\n"
                + "\n"
                + "    def myCallbackFunction(self, value):\n"
                + "        self._gotSensorValue(value)\n"
                + "\n"
                + "class ActuatorStub (SensorBase):\n"
                + "\n"
                + "    def __init__(self, argv, actuator = True, periodic = False):\n"
                + "        super().__init__(argv = argv,  actuator = True,  periodic = False) # python 3 only\n"
                + "\n"
                + "    def _setActuatorValue(self, val):\n"
                + "        print (val.payload)\n"
                + "        print (val.value)\n"
                + "\n"
                + "if __name__ == \"__main__\":\n"
                + "    client = SensorStub(sys.argv)\n"
                + "    client.run()";
        
        String url = "100.70.2.138";
        Integer port = 22;
        String user = "pi";
        //String key = "raspberry";
        
        ScriptDeployer instance = new ScriptDeployer();
        ArrayList<Script>
        
        instance.deployScript("asd",, url, port, user, key);
    }

}
