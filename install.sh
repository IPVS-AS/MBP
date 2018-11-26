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
sudo apt-get install maven;

echo "\nInstalling Mosquitto Broker, MongoDB, Tomcat8, git and maven...\n"
# Install Mosquitto Broker
sudo apt-get install -qy mosquitto;
sudo systemctl start mosquitto;

# Install and start MongoDB 
sudo apt-get -qy install mongodb-server;
sudo systemctl start mongodb;

# Install Tomcat 9
sudo apt-get install -qy tomcat8;
sudo systemctl start tomcat8;

echo "\nBuilding .war file...\n"
sudo mvn clean install

# deploy war to Tomcat
echo "\nDeploying .war file...\n"
sudo mv target/MBP-0.1.war /var/lib/tomcat8/webapps/MBP.war

echo "\nInstallation finished"
