#!/bin/sh

#########################################################################################################################
# This install script provides a fully automated installation of the MBP.                                               #
# It requires systemd as the running init system.                                                                       #
# It installs Java, Mosquitto, MongoDB and Tomcat8 to run the MBP.                                                      #
# Moreover it installs git and maven to build the necessary files.                                                      #
#                                                                                                                       #
# After the installation you will find a folder named connde in the directory the script was started.                   #
# This folder contains all source code of the MBP and can be used to build it.                                          #
#########################################################################################################################

echo "write hostname\n"
sudo sh -c "echo '127.0.0.1' $(hostname) >> /etc/hosts";
echo "\nupdate package repositories\n"
sudo apt-get -qy update;
#sudo apt-get -qy upgrade;

#Installing Java8
echo "\nInstalling Java...\n"
sudo apt-get install -qy openjdk-8-jdk;
sudo apt-get install -qy maven;

echo "\nInstalling Mosquitto Broker, MongoDB, InfluxDB, Tomcat8, git and maven...\n"
# Install Mosquitto Broker
#sudo apt-get install -qy mosquitto;
#sudo systemctl start mosquitto;

# Install and start MongoDB 
sudo apt-get -qy install mongodb-server;
sudo systemctl start mongodb;

# Install and start InfluxDB
curl -sL https://repos.influxdata.com/influxdb.key | sudo apt-key add -;
source /etc/lsb-release;
echo "deb https://repos.influxdata.com/${DISTRIB_ID,,} ${DISTRIB_CODENAME} stable" | sudo tee /etc/apt/sources.list.d/influxdb.list;
sudo apt-get update && sudo apt-get install influxdb;
sudo systemctl unmask influxdb.service;
sudo systemctl start influxdb;

# Install Tomcat 8
sudo apt-get install -qy tomcat8;
echo "\nConfiguring security settings of Tomcat8...\n"
sudo find /usr/lib/jvm -mount -name 'java.security' | while read line; do
    echo "Modifying '$line'"
    sudo sed -i 's|securerandom\.source=file\:/dev/random|securerandom.source=file:/dev/./urandom|g' "$line"
    sudo sed -i 's|securerandom\.source=file\:/dev/urandom|securerandom.source=file:/dev/./urandom|g' "$line"
done

echo "\nStarting Tomcat8\n";
sudo systemctl start tomcat8;

echo "\nBuilding .war file...\n"
sudo mvn clean install

# deploy war to Tomcat
echo "\nDeploying .war file...\n"
sudo mv target/MBP-0.1.war /var/lib/tomcat8/webapps/MBP.war

echo "\nInstallation finished"
