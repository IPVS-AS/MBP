import time
import json
import socket as sck
# sudo pip3 install netifaces
import netifaces as ni
import const
from enum import auto

SLEEPTIME = 2
PORT = 20123
AUTODEPLOY_FILE = 'autodeploy.conf'


class LanAdvertiser:
    def __init__(self):
        print('Created advertiser')
        self.server_address = None
        self.ip = None
        self.mac = None
        self.s = sck.socket(sck.AF_INET, sck.SOCK_DGRAM)
        self.s.setsockopt(sck.SOL_SOCKET, sck.SO_BROADCAST, 1)
        self.s.settimeout(5)

    def connect_device(self, device, own_ip, own_mac, target_address):
        print('Connecting device |' + device[const.NAME])
        data = {
            const.DEV_IP: own_ip,
            const.DEV_MAC: own_mac.replace(':', '').lower(),
            const.DEV_TYPE: device[const.DEV_TYPE],
            const.LOCAL_ID: device[const.NAME],
            const.CONN_TYPE: const.CONN_HELLO
        }
        data = (json.dumps(data))
        print('data: ' + data)
        self.s.sendto(data.encode('utf-8'), target_address)

        try:
            msg, srv_addr = self.s.recvfrom(1024)
            print('rcv: ' + msg.decode('utf-8') + ' from: ' + target_address[0])
            self.server_address = srv_addr
            data = json.loads(msg.decode('utf-8'))
            global_id = data['globalId']

            data = {const.GLOBAL_ID: global_id, const.PINSET: device[const.PINSET]}
            msg = json.dumps(data).encode('utf-8')
            print('Sending adapter config: ' + str(data))

            self.s.sendto(msg, self.server_address)

            connected = True
        except sck.timeout:
            print('timed out')
            connected = False

        return connected

    def discover_server_lan(self):
        print('Discovering server')
        interfaces = ni.interfaces()
        for i in interfaces:  # interface
            if ni.AF_INET in ni.ifaddresses(i):  # if interface has AF_INET (IPv4)
                addrs = ni.ifaddresses(i)[ni.AF_INET]
                mac_addrs = ni.ifaddresses(i)[ni.AF_LINK]
                for addr in addrs:  # list of IPv4 addresses (yes, there's more than one)
                    for mac_addr in mac_addrs:
                        if 'broadcast' in addr and 'addr' in addr:  # if it has a broadcast address
                            bc_addr = addr['broadcast']
                            self.ip = addr['addr']

                            if self.ip == "127.0.0.1":  # omit the loop interface
                                continue

                            self.mac = mac_addr['addr']
                            print('broadcasting to: |' + str(bc_addr) + '|')

                            data = {
                                const.CONN_TYPE: const.CONN_PING,
                                const.PING_MSG: 'ping'
                            }

                            msg = json.dumps(data).encode('utf-8')
                            print('Ping server with |' + msg.decode('utf-8') + '|')
                            self.s.sendto(msg, (bc_addr, PORT))

                            try:
                                msg, srv_addr = self.s.recvfrom(1024)
                                print('Received pong |' + msg.decode('utf-8') + '|')
                                data = json.loads(msg.decode('utf-8'))
                                if const.PING_MSG in data and data[const.PING_MSG] == 'pong':
                                    print('server found at |' + srv_addr + '|')
                                    self.server_address = srv_addr
                                    return True
                            except sck.timeout:
                                print('No response')
        return False

    def advertise(self):
        while self.server_address is None:
            print('advertising client')
            self.discover_server_lan()
            time.sleep(SLEEPTIME)

        print('Server found @ |' + self.server_address + '|')

        if self.server_address is not None:
            for sensor in autodeploy_data['sensors']:  # set invalid id for all devices
                self.connect_device(sensor, self.ip, self.mac, self.server_address)


def getAutodeploy():
    try:
        json_data = open(AUTODEPLOY_FILE).read()
        return json.loads(json_data)
    except IOError:
        return None


def set_unconnected():
    for sensor in autodeploy_data['sensors']:  # set invalid id for all devices
        global_ids[sensor[const.NAME]] = -1

    for actuator in autodeploy_data['actuators']:
        global_ids[actuator[const.NAME]] = -1


def check_connected():
    connected = True
    for sensor in autodeploy_data['sensors']:
        if global_ids.get(sensor[const.NAME]) == -1:
            connected = False

    for actuator in autodeploy_data['actuators']:
        if global_ids.get(actuator[const.NAME]) == -1:
            connected = False

    return connected


if __name__ == '__main__':
    print('Starting client')
    global_ids = dict()
    autodeploy_data = getAutodeploy()
    print('autodeploy data: ' + str(autodeploy_data))

    set_unconnected()
    advertiser = LanAdvertiser()
    print('Start client advertising')
    while not check_connected():
        advertiser.advertise()
