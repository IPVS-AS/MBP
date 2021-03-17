#!/bin/sh

###########################################################################################
# This script removes the software components installed with the MBP installation script. #
# It removes Mosquitto, MongoDB and Tomcat.                                               #
###########################################################################################

echo "This will uninstall the MBP from your device."
read -n1 -p "\nDo you also want to uninstall Tomcat? [y,n]" un_tomcat 
read -n1 -p "\nDo you also want to uninstall MongoDB? [y,n]" un_mongo
read -n1 -p "\nDo you also want to uninstall mosquitto? [y,n]" un_mosquitto

echo "Stopping Tomcat..."
sudo systemctl stop tomcat*

echo "Removing MBP..."
sudo rm /var/lib/tomcat8/webapps/MBP.war
sudo rm -rf /var/lib/tomcat8/webapps/MBP


# Uninstall Tomcat if desired
if [[ $un_tomcat == "Y" || $un_tomcat == "y" ]]; then
	echo "Uninstalling Tomcat..."
	sudo systemctl stop tomcat*
	sudo apt-get remove -qy tomcat*
	sudo apt purge -qy tomcat*
fi


# Uninstall MongoDB if desired
if [[ $un_mongo == "Y" || $un_mongo == "y" ]]; then
	echo "Uninstalling MongoDb..."
	sudo systemctl stop mongodb
	sudo apt-get remove -qy mongodb-server
	sudo apt purge -qy mongodb-server
	sudo rm -r /var/log/mongodb
	sudo rm -r /var/lib/mongodb
fi


# Uninstall mosquitto if desired
if [[ $un_mosquitto == "Y" || $un_mosquitto == "y" ]]; then
	echo "Uninstalling mosquitto..."
	sudo systemctl stop mosquitto
	sudo apt-get remove -qy mosquitto
	sudo apt purge -qy mosquitto
fi


echo "Uninstalling completed."
