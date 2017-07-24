# message parameters
GLOBAL_ID = 'global_id'
DEV_IP = 'dev_ip'
DEV_HW_ADDRESS = 'dev_hw_addr'
LOCAL_ID = 'local_id'
TYPE = 'type'
CONN_TYPE = 'conn_type'
PING_MSG = 'ping_msg'

# Connection types
CONN_HELLO = 'hello'
CONN_INIT = 'init'
CONN_VALUE = 'value'
CONN_KEEP_ALIVE = 'alive'
CONN_PING = 'ping'

# autodeploy parameter
DEPLOY_SENSORS = 'sensors'
DEPLOY_ADAPTERS = 'adapters'
ADAPTER_CONF = 'adapter_conf'
NAME = 'name'
PINSET = 'pinset'
DEV_TYPE = 'type'

# other stuff
ENCODING = 'utf-8'
LAST_CONTACT = 'last_contact'

# server constants
DB_NAME = 'discovery'
DEV_COLL_NAME = 'devices'
STATUS_COLL_NAME = 'status'

AUTODEPLOY_URL = 'http://localhost:8080/connde/api/autodeploy'

STATUS_NEXT_ID = 'next_id'
STATUS_FOR = 'status_for'

SERVER_SERVICE = 'service'
SERVER_LAN = 'lan'
SERVER_BT = 'bt'

# bluetooth server constants
BT_UUID = '809a061f-d834-4ba6-8741-29f0f53ca1b9'
BT_SERVICE_NAME = 'connde_discovery'
BT_SERVICE_DESCRIPTION = 'Server used for device discovery in the connde application'

# Comm types
LAN = 'LAN'
BT = 'BT'

# client constants
SLEEPTIME = 2
PORT = 20123
AUTODEPLOY_FILE = 'autodeploy.json'
CLIENT_TIMEOUT = 5
