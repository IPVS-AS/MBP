import logging as log
import bluetooth

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

print('Awaiting connections on RFCOMM channel %d', bt_sck.getsockname())

client_sck, client_info = bt_sck.accept()

print('Accepted connection from ', client_info)

print('Disconnecting...')
client_sck.close()
bt_sck.close()
