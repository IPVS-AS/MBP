import time
import json
import socket as sck
import fcntl, struct
# sudo pip3 install netifaces
import netifaces as ni

SLEEPTIME = 10
PORT = 20123
AUTODEPLOY_FILE = '/etc/connde/autodeploy.conf'

def getAutodeploy():
    try:
        json_data = open(AUTODEPLOY_FILE).read()
        data = json.loads(json_data)
        return data['autodeploy']
    except:
        return False

interfaces = ni.interfaces()

for i in interfaces: # interface
    if (ni.AF_INET in ni.ifaddresses(i)): # if interface has AF_INET (IPv4)
        addrs = ni.ifaddresses(i)[ni.AF_INET]
        mac_addrs = ni.ifaddresses(i)[ni.AF_LINK] 
        for addr in addrs: # list of IPv4 addresses (yes, there's more than one)
            for mac_addr in mac_addrs:
                if ('broadcast' in addr and 'addr' in addr): # if it has a broadcast address
                    bc_addr = addr['broadcast']
                    ip = addr['addr']
                    mac = mac_addr['addr']
                    print ('broadcasting to: ' + str(bc_addr))
                    s = sck.socket(sck.AF_INET, sck.SOCK_DGRAM)
                    s.setsockopt(sck.SOL_SOCKET, sck.SO_BROADCAST, 1)

                    data = { 'iface': i, 'ipAddress': ip, 'macAddress': mac.replace(':', '').lower(),
                            'autodeploy': getAutodeploy() }
                    data = (json.dumps(data))
                    print ('data: ' + data)
                    s.sendto((data).encode('utf-8'), (bc_addr, PORT))