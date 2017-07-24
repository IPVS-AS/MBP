import logging as log
import bluetooth

log.basicConfig(format='%(asctime)s |%(levelname)s|:%(message)s', level=log.DEBUG)

UUID = '809a061f-d834-4ba6-8741-29f0f53ca1b9'
SERVICE_NAME = 'connde_discovery'
SERVICE_DESCRIPTION = 'Server used for device discovery in the connde application'

bt_sck = bluetooth.BluetoothSocket()
bt_sck.bind(('', bluetooth.PORT_ANY))
bt_sck.listen(1)
bluetooth.advertise_service(bt_sck,
                            SERVICE_NAME,
                            service_id=UUID,
                            service_classes=[UUID, bluetooth.SERIAL_PORT_CLASS],
                            profiles=[bluetooth.SERIAL_PORT_PROFILE],
                            description=SERVICE_DESCRIPTION,
                            protocols=[bluetooth.RFCOMM_UUID]
                            )

log.info('Releasing system resources (shutting down)')

bt_sck.close()
