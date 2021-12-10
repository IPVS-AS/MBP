#!/usr/bin/env python3
import paho.mqtt.client as mqtt
import configparser
import json
import os

################# Configuration #####################
MBP_CLIENT_PROPERTIES_FILE = 'mbp.properties'
SECTION_MBP = 'MBP'
SECTION_MBP_PROPERTY_BROKERHOST = 'brokerHost'
SECTION_MBP_PROPERTY_BROKERPORT = 'brokerPort'
SECTION_MBP_PROPERTY_BROKERTOPIC = 'brokerTopic'
SECTION_MBP_PROPERTY_BROKERACTIONTOPIC = 'brokerActionTopic'
SECTION_COMPONENT = 'Component'
SECTION_COMPONENT_PROPERTY_COMPONENTID = 'componentId'

TOPIC_SEND_MESSAGE_FORMAT = '{"component": "SENSOR", "id": "%s", "value": %s}'

ACTION_LOG_FILE = 'actions.log'
JSON_PROPERTY_ACTION = 'action'
ACTION_NAME_STOP = 'stop'
#####################################################
PAR_FIELD_NAME = "name"
PAR_FIELD_VALUE = "value"
#####################################################
class MBPclient(object):
  def __init__(self):
    # Get MQTT broker connection information
    self.__get_mqtt_broker_infos()

  def connect(self):
    # create the MQTT client instance
    self.client_id = 'mbp-%s' % (self.component_id)
    self.mqtt_client = mqtt.Client(client_id=self.client_id, clean_session=True, userdata=None, protocol=mqtt.MQTTv31)

    # set mqtt client on_connection callback
    self.mqtt_client.on_connect = self._on_connect
    self.mqtt_client.on_message = self._on_message

    self.mqtt_client.connect(self.broker_host, self.broker_port, keepalive=60)

    # Runs a thread in the background to call loop() automatically.
    # This call also handles reconnecting to the broker.
    self.mqtt_client.loop_start()

  def send_data(self, value):
    mbp_message = TOPIC_SEND_MESSAGE_FORMAT % (self.component_id, value)
    self.mqtt_client.publish(topic=self.broker_topic, payload=mbp_message, qos=0, retain=False)
    print('[Sent message]:', mbp_message)

  def subscribe(self, topic):
    self.mqtt_client.subscribe(topic)

  def finalize(self):
    self.mqtt_client.loop_stop()

  def get_start_par_value(self, input_args, par_name, default_value):
    if input_args:
      paramArray = json.loads(input_args[0])
      for param in paramArray:
        if not (PAR_FIELD_NAME in param and PAR_FIELD_VALUE in param):
          continue
        elif param[PAR_FIELD_NAME].lower() == par_name.lower():
          if param[PAR_FIELD_VALUE] is not None:
            return param[PAR_FIELD_VALUE]

    print("[ParameterParsing]: no match found between received and expected parameters. Using default value.")
    return default_value

  def __get_mqtt_broker_infos(self, property_file_name=MBP_CLIENT_PROPERTIES_FILE):
    """Retrieves the MQTT broker information to connect and send data to it."""

    config = configparser.RawConfigParser()
    config.read(property_file_name)
    self.broker_host = config.get(SECTION_MBP, SECTION_MBP_PROPERTY_BROKERHOST)
    self.broker_port = int(config.get(SECTION_MBP, SECTION_MBP_PROPERTY_BROKERPORT))
    self.broker_topic = config.get(SECTION_MBP, SECTION_MBP_PROPERTY_BROKERTOPIC)
    self.broker_action_topic = config.get(SECTION_MBP, SECTION_MBP_PROPERTY_BROKERACTIONTOPIC)
    self.component_id = config.get(SECTION_COMPONENT, SECTION_COMPONENT_PROPERTY_COMPONENTID)

  def _on_connect(self, client, userdata, flags, rc):
    """This callback function is executed when the MQTT client receives a CONNACK response from the MQTT broker."""

    print('[Connected]: client ID', self.client_id, 'result code', str(rc))
    self.subscribe(self.broker_action_topic)

  def _on_message(self, client, userdata, message):

    # Convert message payload to string
    message_string = message.payload.decode(encoding='UTF-8')

    # Open actions log file and append message
    with open(ACTION_LOG_FILE, 'a') as file:
      file.write(message_string)
      file.write('\n')

    msg_json = json.loads(message_string)
    msg_data = msg_json[JSON_PROPERTY_ACTION]

    if (msg_data is not None) and (msg_data.casefold() == ACTION_NAME_STOP.casefold()):
      print('[Exit]: Receive command to exit MBP client')
      self.finalize()
      os._exit(0)
