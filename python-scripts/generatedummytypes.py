from pymongo import MongoClient
import const

client = MongoClient()
db_connde = client.get_database(const.CONNDE_DB_NAME)
db_discovery = client.get_database(const.DB_NAME)

coll_connde_sensor = db_connde.get_collection(const.CONNDE_SENSOR_COLLECTION)
coll_connde_device = db_connde.get_collection(const.CONNDE_DEVICE_COLLECTION)
coll_connde_actuator = db_connde.get_collection(const.CONNDE_ACTUATOR_COLLECTION)
coll_connde_type = db_connde.get_collection(const.CONNDE_TYPE_COLLECTION)

coll_discovery_devices = db_discovery.get_collection(const.DEV_COLL_NAME)

discovered_devices = coll_discovery_devices.find({})
for device in discovered_devices:
    typeName = device[const.TYPE]
    connde_type = coll_connde_type.find_one({const.CONNDE_TYPE_NAME: typeName})
    if connde_type is None:
        dummy_type = {
            const.CONNDE_TYPE_CLASS: const.CONNDE_TYPE_JAVA_CLASS,
            const.CONNDE_TYPE_NAME: typeName,
            const.CONNDE_TYPE_DESCIRPTION: 'dummy type',
            const.CONNDE_TYPE_SERVICE: '',
            const.CONNDE_TYPE_ROUTINES: '',
        }
        print('Dummy ' + str(dummy_type))
        coll_connde_type.insert_one(dummy_type)
        print('inserted')
    else:
        print('Real' + str(connde_type))

client.close()