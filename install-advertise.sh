#!/bin/sh

INSTALL_PATH=/opt/rmp
CONF_PATH=/etc/rmp

# clone repository
echo "\nCloning repository...\n"
sudo apt-get install -qy git
CLONE_LOCATION=/tmp/connde-clone
git clone https://github.com/rossojo/connde $CLONE_LOCATION

# install advertise scripts and conf file
echo "\nInstalling advertise service...\n"
cd $CLONE_LOCATION/python-scripts
sudo cp rmpdiscovery rmpadvertsie.py rmpadvertise.sh $INSTALL_PATH
sudo chmod 775 $INSTALL_PATH/rmpadvertise.sh
sudo cp autodeploy.json $CONF_PATH
sudo cp rmpadvertise.service /etc/systemd/system

# cleanup
echo "\nCleaning up...\n"
rm -rf $CLONE_LOCATION
