# noinspection PyUnresolvedReferences
from ..const import *

DISCOVERY_DB_NAME = 'discovery'
MONITOR_COLL_NAME = 'monitoring'
DEV_COLL_NAME = 'devices'
STATUS_COLL_NAME = 'status'
DB_NAME = 'discovery'

AUTODEPLOY_URL = 'http://localhost:8080/connde/api/autodeploy'

STATUS_NEXT_ID = 'next_id'
STATUS_FOR = 'status_for'

MONITORING = 'monitoring'

SERVER_MONITOR_SLEEP = 5

SERVER_SERVICE = 'service'
SERVER_LAN = 'lan'
SERVER_BT = 'bt'

RMP_DB_NAME = 'connde'
RMP_DEVICE_COLLECTION = 'device'
RMP_SENSOR_COLLECTION = 'sensor'
RMP_ACTUATOR_COLLECTION = 'actuator'
RMP_TYPE_COLLECTION = 'type'

RMP_DEVICE_NAME = 'name'
RMP_DEVICE_IFAC = 'iface'
RMP_DEVICE_MAC = 'macAddress'
RMP_DEVICE_IP = 'ipAddress'
RMP_DEVICE_AUTODEPLOY = 'autodeploy'
RMP_DEVICE_DATE = 'date'

RMP_TYPE_NAME = 'name'
CONNDE_TYPE_CLASS = '_class'
CONNDE_TYPE_JAVA_CLASS = 'org.citopt.connde.domain.type.Type'
CONNDE_TYPE_DESCIRPTION = 'description'
CONNDE_TYPE_SERVICE = 'service'
CONNDE_TYPE_ROUTINES = 'routines'

RMP_SENSOR_CLASS = '_class'
CONNDE_SENSOR_JAVA_CLASS = 'org.citopt.connde.domain.component.Sensor'
RMP_SENSOR_NAME = 'name'
RMP_SENSOR_TYPE = 'type'
RMP_SENSOR_DEVICE = 'device'

CONNDE_ID = 'conndeId'
MONGO_ID = '_id'

# bluetooth
BT_SERVICE_NAME = 'connde_discovery'
BT_SERVICE_DESCRIPTION = 'Server used for device discovery in the connde application'

# command line arguments
SERVER_CMD_LAN = 'startLan'
SERVER_CMD_BT = 'startBt'
