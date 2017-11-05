#!/bin/sh

#########################################################################################################################
# This install script provides a fully automated installation of the RMP.						#
# It requires systemd as the running init system.									#
# It installs Java, Python, Mosquitto, MongoDB and Tomcat8 to run the RMP.						#
# Moreover it installs git and maven to build the necessary files.							#
#															#
# After the installation you will find a folder named connde in the directory the script was started.			#	
# This folder contains all source code of the RMP and can be used to build it.						#	
# More over the necessary python scripts, running the discovery service and the value logger will be installed using a 	#
# virtual Python environment.												#
# The environment and configuration files will be stored in /etc/rmp							#
# while the scripts are installed at /opt/rmp										#
#########################################################################################################################

echo "write hostname"
sudo sh -c "echo '127.0.0.1' $(hostname) >> /etc/hosts";
echo "update package repositories"
sudo apt-get -qy update;
#sudo apt-get -qy upgrade;

#Installing Java8, pip
echo "Installing Java and pip..."
sudo apt-get install -qy openjdk-8-jdk;
sudo apt-get install -qy python-pip;

# Install python virtual environment and install python packages
echo "Installing python packages..."
sudo -H pip install -r python-packages.txt

echo "Installing Mosquitto Broke, MongoDB, Tomcat8, git and maven..."
# Install Mosquitto Broker
sudo apt-get install -y mosquitto;
sudo systemctl start mosquitto;

# Install and start MongoDB 
sudo apt-get -y install mongodb-server;
sudo systemctl start mongodb;
#sudo systemctl status mongodb;

#sudo /etc/init.d/mongodb start;
#sudo /etc/init.d/mongodb status;

# Install Tomcat 9
sudo apt-get install -y tomcat8;
sudo systemctl start tomcat8;
sudo apt-get install -y git;
sudo apt-get install -y maven;

# checkout repository
echo "Checking out the repository"
sudo git clone https://github.com/rossojo/connde.git
cd connde/java-projects/restful-connde
echo "Building .war file..."
sudo mvn clean install

# deploy war to Tomcat
echo "Deploying .war file..."
sudo mv target/restful-connde-1.0-SNAPSHOT.war /var/lib/tomcat8/webapps/MBP.war

# Install discovery service
echo "Installing python scripts..."
sudo mkdir -p /opt/rmp
sudo mkdir -p /etc/rmp
cd ../../python-scripts
sudo cp -r rmpdiscovery rmpdiscovery.py rmpdiscovery.sh rmpadvertise.sh rmpadvertise.py /opt/rmp
sudo cp rmpdiscovery.service rmpadvertise.service /etc/systemd/system
sudo cp value-logger.py /opt/rmp
echo "Installation finished"
