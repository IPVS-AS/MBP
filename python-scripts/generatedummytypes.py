from pymongo import MongoClient
import discovery.discoveryconst as const
import requests

url = "http://localhost:8080/MBP/api/types"

client = MongoClient()
db_connde = client.get_database(const.RMP_DB_NAME)
db_discovery = client.get_database(const.DB_NAME)

coll_connde_sensor = db_connde.get_collection(const.RMP_SENSOR_COLLECTION)
coll_connde_device = db_connde.get_collection(const.RMP_DEVICE_COLLECTION)
coll_connde_actuator = db_connde.get_collection(const.CONNDE_ACTUATOR_COLLECTION)
coll_connde_type = db_connde.get_collection(const.RMP_TYPE_COLLECTION)

coll_discovery_devices = db_discovery.get_collection(const.DEV_COLL_NAME)

discovered_devices = coll_discovery_devices.find({})
for device in discovered_devices:
    typeName = device[const.DEV_TYPE]
    connde_type = coll_connde_type.find_one({const.RMP_TYPE_NAME: typeName})
    if connde_type is None:
        dummy_type = {
            const.RMP_TYPE_NAME: typeName,
            const.CONNDE_TYPE_DESCIRPTION: 'dummy type',
            const.CONNDE_TYPE_SERVICE: {
                "name": typeName + "_service",
                "content": "stub"
            },
            const.CONNDE_TYPE_ROUTINES: [{
                "name": typeName + "_routine",
                "content": "stub"
            }]
        }
        print('Dummy ' + str(dummy_type))
        r = requests.post(url, json=dummy_type)
        print(r)
        print(r.content)
    else:
        print('Real' + str(connde_type))

client.close()