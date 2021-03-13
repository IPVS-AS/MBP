#!/bin/sh

#########################################################################################################################
# This install script provides a fully automated installation of the MBP.                                               #
# It requires systemd as the running init system.                                                                       #
# It installs Java, Mosquitto, MongoDB and Tomcat8 to run the MBP.                                                      #
# Moreover it installs maven to build the necessary files.                                                              #
#########################################################################################################################

echo "write hostname\n"
sudo sh -c "echo '127.0.0.1' $(hostname) >> /etc/hosts";
echo "\nupdate package repositories\n"
sudo apt-get -qy update;
sudo apt-get -qy upgrade;

#Installing Java8
echo "\nInstalling Java...\n"
sudo apt-get install -qy openjdk-8-jdk;
sudo apt-get install -qy maven;

echo "\nInstalling Mosquitto Broker, MongoDB, Tomcat8, git and maven...\n"

if [ -n "$1" ]
then
    if [ "$1" = "secure" ] 
    then
        echo "Installing secured Mosquitto with OAuth2 authentication as Docker container..."
        echo "\nbroker_location=LOCAL_SECURE" >> src/main/resources/config.properties
        echo "\nBuilding mosquitto with go-auth plugin...\n"
        cd mosquitto/  
        docker build -t mosquitto-go-auth .
        echo "\nStarting docker container for mosquitto with go-auth plugin...\n"
        docker run -d --network="host" -p 1883:1883 -p 1884:1884 mosquitto-go-auth
        cd ..
    else
        echo "Invalid command-line argument(s)."
    fi
else
    echo "Installing normal Mosquitto..."
    sudo apt-get install -qy mosquitto;
    sudo systemctl start mosquitto;
fi

# Install and start MongoDB 
sudo apt-get -qy install mongodb-server;
sudo systemctl start mongodb;

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

# Deploy Web Application Archive to Tomcat (using wildcards for file name)
echo "\nDeploying .war file...\n"
find target/ -name "MBP-*.war" -print0 | xargs -0 -I {} sudo mv {} /var/lib/tomcat8/webapps/MBP.war

# retrieve status
MOSQUITTO_STATUS=$(systemctl is-active mosquitto)
MONGODB_STATUS=$(systemctl is-active mongodb)
TOMCAT_STATUS=$(systemctl is-active tomcat*)
WAR_FILE_STATUS="undeployed"
if [ -e /var/lib/tomcat*/webapps/MBP.war ]
then
  WAR_FILE_STATUS="deployed"
fi

# output status
echo "---------------------------"
echo "Software components status:"
echo "> Mosquitto: $MOSQUITTO_STATUS"
echo "> MongoDB: $MONGODB_STATUS"
echo "> Tomcat: $TOMCAT_STATUS"
echo "> MBP WAR file: $WAR_FILE_STATUS"
echo "---------------------------------------------------------"

if [ $MOSQUITTO_STATUS = "active" ] && [ $MONGODB_STATUS = "active" ] && [ $TOMCAT_STATUS = "active" ] && [ $WAR_FILE_STATUS = "deployed" ]
then
  echo "Installation finished SUCCESSFULLY!"
  echo "The MBP should be running on http://localhost:8080/MBP"
else
  echo "Installation INCOMPLETED!"
  echo "At least one of the required components is not active"
fi
echo "---------------------------------------------------------"
