#!/bin/sh

###########################################################################################
# This script removes the MBP application and un-registers the corresponding service.     #
# It can also be used to uninstall mosquitto and MongoDB.                                    #
###########################################################################################

# Config
TARGET_DIR="/usr/local/bin/MBP"
SERVICE_NAME="mbp"

echo "This will uninstall the MBP from your device."
read -n1 -p "Do you also want to uninstall MongoDB? [y,n]" un_mongo
echo ""
read -n1 -p "Do you also want to uninstall mosquitto? [y,n]" un_mosquitto
echo ""

echo "Stopping the MBP application..."
sudo systemctl stop $SERVICE_NAME

echo "Removing the MBP application..."
sudo rm -rf $TARGET_DIR

echo "Unregistering service..."
sudo rm /etc/systemd/system/${SERVICE_NAME}.service
sudo systemctl daemon-reload

# Uninstall MongoDB if desired
if [[ $un_mongo == "Y" || $un_mongo == "y" ]]; then
	echo "Uninstalling MongoDB..."
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

echo "Uninstalled the MBP and related components SUCCESSFULLY!"
