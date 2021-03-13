#!/bin/sh

###########################################################################################
# This script removes the software components installed with the MBP installation script. #
# It removes Mosquitto, MongoDB and Tomcat.                                               #
###########################################################################################

echo "Stopping tomcat, mosquitto and mongodb"
sudo systemctl stop tomcat*
sudo systemctl stop mosquitto
sudo systemctl stop mongodb

echo "Uninstalling tomcat"
sudo apt-get remove -qy tomcat*
sudo apt purge -qy tomcat*

echo "Uninstalling mosquitto"
sudo apt-get remove -qy mosquitto
sudo apt purge -qy mosquitto

echo "Uninstalling mongodb"
sudo apt-get remove -qy mongodb-server
sudo apt purge -qy mongodb-server
sudo rm -r /var/log/mongodb
sudo rm -r /var/lib/mongodb

echo "Uninstalling completed."
