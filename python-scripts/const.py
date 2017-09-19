# message parameters
GLOBAL_ID = 'global_id'
DEV_IP = 'dev_ip'
DEV_HW_ADDRESS = 'dev_hw_addr'
LOCAL_ID = 'local_id'
TYPE = 'type'
CONN_TYPE = 'conn_type'
PING_MSG = 'ping_msg'
HOST = 'host'
ERROR = 'error'

# Connection types
CONN_HELLO = 'hello'
CONN_INIT = 'init'
CONN_VALUE = 'value'
CONN_KEEP_ALIVE = 'alive'
CONN_PING = 'ping'

# autodeploy parameter
DEPLOY_SELF = 'self'
DEPLOY_ON = 'on'
DEPLOY_DEVICES = 'devices'
ADAPTER_CONF = 'adapter_conf'
PINSET = 'pinset'
DEV_TYPE = 'type'
TIMEOUT = 'timeout'

# other stuff
ENCODING = 'utf-8'
LAST_CONTACT = 'last_contact'

# server constants
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

CONNDE_DB_NAME = 'connde'
CONNDE_DEVICE_COLLECTION = 'device'
CONNDE_SENSOR_COLLECTION = 'sensor'
CONNDE_ACTUATOR_COLLECTION = 'actuator'
CONNDE_TYPE_COLLECTION = 'type'

CONNDE_DEVICE_NAME = 'name'
CONNDE_DEVICE_IFAC = 'iface'
CONNDE_DEVICE_MAC = 'macAddress'
CONNDE_DEVICE_IP = 'ipAddress'
CONNDE_DEVICE_AUTODEPLOY = 'autodeploy'
CONNDE_DEVICE_DATE = 'date'

CONNDE_TYPE_NAME = 'name'
CONNDE_TYPE_CLASS = '_class'
CONNDE_TYPE_JAVA_CLASS = 'org.citopt.connde.domain.type.Type'
CONNDE_TYPE_DESCIRPTION = 'description'
CONNDE_TYPE_SERVICE = 'service'
CONNDE_TYPE_ROUTINES = 'routines'

CONNDE_SENSOR_CLASS = '_class'
CONNDE_SENSOR_JAVA_CLASS = 'org.citopt.connde.domain.component.Sensor'
CONNDE_SENSOR_NAME = 'name'
CONNDE_SENSOR_TYPE = 'type'
CONNDE_SENSOR_DEVICE = 'device'
# bluetooth server constants
BT_UUID = '809a061f-d834-4ba6-8741-29f0f53ca1b9'
BT_SERVICE_NAME = 'connde_discovery'
BT_SERVICE_DESCRIPTION = 'Server used for device discovery in the connde application'

# Comm types
LAN = 'LAN'
BT = 'BT'
TEST = 'TEST'

# client constants
SLEEPTIME = 2
PORT = 20123
AUTODEPLOY_FILE = 'autodeploy.json'
CLIENT_TIMEOUT = 5

CLIENT_CMD_ARG = 'advertiser_class'

GLOBAL_ID_FILE = 'global_ids.json'
