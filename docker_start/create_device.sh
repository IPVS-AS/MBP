#!/bin/bash
echo "Start create_device.sh"
until $(curl --output /dev/null --silent --head --fail http://localhost:8080/mbp); do
    printf '.'
    sleep 5
done
echo "Setup the MBP with testing VM as IoT environment"
curl -c cookies.txt -X POST http://localhost:8080/mbp/api/users/login -H "X-MBP-Access-Request: requesting-entity-firstname=admin;;requesting-entity-lastname=admin;;requesting-entity-username=admin" -H "Content-Type: application/json" -d "{\"username\":\"admin\",\"password\":\"12345\"}"
curl -b cookies.txt -X POST http://localhost:8080/mbp/api/settings -H "X-MBP-Access-Request: requesting-entity-firstname=admin;;requesting-entity-lastname=admin;;requesting-entity-username=admin" -H "Content-Type: application/json" -d "{\"senderName\":\"MBP\",\"brokerLocation\":\"REMOTE\",\"brokerIPAddress\":\"172.16.238.3\",\"brokerPort\":1883,\"demoMode\":false}"
curl -b cookies.txt -X POST http://localhost:8080/mbp/api/devices -H "X-MBP-Access-Request: requesting-entity-firstname=admin;;requesting-entity-lastname=admin;;requesting-entity-username=admin" -H "Content-Type: application/json" -d "{\"username\":\"mbp\",\"password\":\"password\",\"ipAddress\": \"172.16.238.10\",\"componentType\":\"Virtual Machine\",\"name\":\"TestDevice (usr=mbp, pwd=password)\",\"errors\":{}}"
echo "Finished create_device.sh"
